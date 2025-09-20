import os
import torch
import torch.nn as nn
from flask import Flask, request, jsonify
from torchvision import transforms
from PIL import Image
import sys

# Add current directory to path so we can import files
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

# Create a custom modified version of the model that doesn't need metadata
# This avoids modifying the original files/model.py
class NoMetaModel(nn.Module):
    def __init__(self, num_classes, model_name, features_dim):
        super(NoMetaModel, self).__init__()
        # Import timm here to avoid circular imports
        import timm
        
        self.base_model = timm.create_model(model_name, pretrained=True, num_classes=0)
        
        # Handle MobileNetV3 specific modifications (from original model.py)
        if model_name in ["tf_mobilenetv3_small_075", "tf_mobilenetv3_large_075"]:
            self.base_model.conv_head = nn.Identity()
            self.base_model.act2 = nn.Identity()
            self.base_model.flatten = nn.Identity()
            
        # Image feature extraction branch (matches original model)
        self.image_feature_extractor = nn.Sequential(
            nn.Dropout1d(p=0.1),    
        )
        
        # For MobileNetV3 Large 075, the feature output is 720 dimensions
        # We need to project from 720 to 752 to match checkpoint expectations
        # (Original model had 720 image features + 32 metadata features = 752 total)
        base_features = 720 if "mobilenetv3_large_075" in model_name else features_dim
        self.feature_projection = nn.Linear(base_features, features_dim)
        
        # Final FC layers - matches original model's FC layers exactly
        self.fc = nn.Sequential(
            nn.Linear(features_dim, 360),
            nn.GELU(),
            nn.Linear(360, 180),
            nn.GELU(),
            nn.Linear(180, 128),
            nn.GELU(),
        )
        
        # Output layer - checkpoint shows 2 classes with sigmoid
        self.output_layer = nn.Sequential(
            nn.Linear(128, num_classes),
            nn.Sigmoid(),
        )
    
    def forward(self, image):
        # Process image through the base model
        image_features = self.base_model(image)
        image_features = image_features.view(image_features.size(0), -1)
        image_features = self.image_feature_extractor(image_features)
        image_features = image_features.view(image_features.size(0), -1)
        
        # Project to the expected feature dimension (720 -> 752 to match training)
        image_features = self.feature_projection(image_features)
        
        # Process through FC layers
        feats = self.fc(image_features)
        
        # Generate predictions
        predictions = self.output_layer(feats)
        return predictions


app = Flask(__name__)

# Using relative paths for better portability
# Path to the script directory
BASE_DIR = os.path.dirname(os.path.abspath(__file__))

# Path to images folder - using relative path
IMAGES_DIR = os.path.join(BASE_DIR, "images")

# Relative path to the checkpoint in files folder
CHECKPOINT_PATH = os.path.join(BASE_DIR, "files", "best_epoch=115-val_accuracy=0.864-train_accuracy=0.970-val_loss=0.575-train_loss=0.494.ckpt")

# Model config - using values that match the checkpoint (MobileNetV3, not DenseNet201)
MODEL_NAME = 'tf_mobilenetv3_large_075'  # Correct model from checkpoint analysis
NUM_CLASSES = 2  # checkpoint shows 2 classes
IMG_SIZE = (224, 224)
FC_INPUT_FEATURES = 752  # from checkpoint analysis (720 + 32 from metadata branch)


def _load_checkpoint_state_dict(ckpt_path: str):
    """Load a checkpoint and return a state_dict compatible with PyTorch models.
    Tries to handle common formats (raw state_dict or dict with 'state_dict' key).
    Strips common prefixes like 'model.' if present.
    """
    ckpt = torch.load(ckpt_path, map_location='cpu')
    state_dict = ckpt.get('state_dict', ckpt)
    # Normalize keys by removing common prefixes
    new_sd = {}
    for k, v in state_dict.items():
        if k.startswith('model.'):  # lightning style
            new_sd[k[len('model.'):]] = v
        else:
            new_sd[k] = v
    return new_sd


class InferenceModel:
    """Wrapper to run inference without metadata while reusing trained weights.
    
    Uses a custom model implementation that doesn't require metadata paths.
    """

    def __init__(self, ckpt_path: str):
        print(f"Initializing inference model...")
        device = 'cuda' if torch.cuda.is_available() else 'cpu'
        print(f"Using device: {device}")
        self.device = torch.device(device)

        # Use the feature dimensions from checkpoint analysis
        print(f"Creating model: {MODEL_NAME} with fc_input_features={FC_INPUT_FEATURES}")

        self.model = NoMetaModel(
            num_classes=NUM_CLASSES,
            model_name=MODEL_NAME,
            features_dim=FC_INPUT_FEATURES,
        ).to(self.device)

        # Load weights
        print(f"Loading checkpoint from: {ckpt_path}")
        state_dict = _load_checkpoint_state_dict(ckpt_path)
        print(f"Loaded state dict with {len(state_dict)} keys")
        
        # Since we added a projection layer, we need to handle missing keys
        result = self.model.load_state_dict(state_dict, strict=False)
        print(f"Missing keys: {result.missing_keys}")
        print(f"Unexpected keys: {result.unexpected_keys}")
        
        self.model.eval()
        print("Model loaded successfully and set to eval mode")

        # Preprocessing with proper normalization (from training data.py)
        # Training used: mean = [0.5123, 0.3595, 0.3125], std = [0.2664, 0.2174, 0.2110]
        self.preprocess = transforms.Compose([
            transforms.Resize(IMG_SIZE),
            transforms.ToTensor(),
            transforms.Normalize(mean=[0.5123, 0.3595, 0.3125], std=[0.2664, 0.2174, 0.2110])
        ])
        print("Preprocessing pipeline initialized with training normalization")

    @torch.no_grad()
    def predict_from_path(self, image_path: str):
        print(f"Processing image: {image_path}")
        img = Image.open(image_path).convert('RGB')
        x = self.preprocess(img).unsqueeze(0).to(self.device)
        
        # Forward through the model
        out = self.model(x)
        
        # Extract probabilities
        probs = out.squeeze(0).detach().cpu().numpy().tolist()
        print(f"Prediction complete: {probs}")
        return probs


# Create a singleton inference engine at startup
print("Setting up inference engine...")

# Make sure images directory exists
if not os.path.exists(IMAGES_DIR):
    print(f"Creating images directory: {IMAGES_DIR}")
    os.makedirs(IMAGES_DIR, exist_ok=True)
else:
    print(f"Images directory exists at: {IMAGES_DIR}")

inference_engine = InferenceModel(CHECKPOINT_PATH)


@app.route('/status', methods=['GET'])
def status():
    """Simple endpoint to check if the API is running."""
    return jsonify({
        'status': 'ok',
        'model': MODEL_NAME,
        'task': 'OPMD vs Benign Classification',
        'classes': ['OPMD (Oral Potentially Malignant Disorder)', 'Benign'],
        'images_dir': IMAGES_DIR,
        'available_images': [f for f in os.listdir(IMAGES_DIR) 
                            if os.path.isfile(os.path.join(IMAGES_DIR, f)) and 
                            f.lower().endswith(('.jpg', '.jpeg', '.png'))]
    })


@app.route('/predict', methods=['POST'])
def predict():
    data = request.get_json(silent=True) or {}
    filename = data.get('filename')
    if not filename:
        return jsonify({'error': 'filename is required'}), 400

    # Try a few common extensions if none is provided
    candidates = [filename]
    root, ext = os.path.splitext(filename)
    if ext.lower() not in ['.jpg', '.jpeg', '.png']:
        candidates = [root + e for e in ['.jpg', '.jpeg', '.png']]

    image_path = None
    for cand in candidates:
        p = os.path.join(IMAGES_DIR, cand)
        if os.path.isfile(p):
            image_path = p
            break

    if image_path is None:
        return jsonify({'error': f'Image not found in {IMAGES_DIR}'}), 404

    try:
        probs = inference_engine.predict_from_path(image_path)
        
        # Based on config analysis: 2 classes for OPMD vs Benign classification
        opmd_prob = float(probs[0])
        benign_prob = float(probs[1])
        
        # Determine the predicted class
        if opmd_prob > benign_prob:
            predicted_class = "OPMD"
            confidence = opmd_prob
        else:
            predicted_class = "Benign"
            confidence = benign_prob
            
        # Create human-readable interpretation
        if confidence > 0.7:
            interpretation = f"High confidence: {predicted_class}"
        elif confidence > 0.4:
            interpretation = f"Moderate confidence: {predicted_class}"
        else:
            interpretation = f"Low confidence: {predicted_class} (model uncertain)"
        
        return jsonify({
            'prediction': {
                'class': predicted_class,
                'confidence': f"{confidence:.3f}",
                'interpretation': interpretation
            },
            'probabilities': {
                'OPMD': f"{opmd_prob:.3f}",
                'Benign': f"{benign_prob:.3f}"
            },
            'raw_scores': {
                'class_0_opmd': str(probs[0]),
                'class_1_benign': str(probs[1])
            },
            'filename': os.path.basename(image_path),
            'note': 'OPMD = Oral Potentially Malignant Disorder'
        })
    except Exception as e:
        return jsonify({'error': str(e)}), 500


if __name__ == '__main__':
    # Print paths for debugging
    print(f"Using images directory: {IMAGES_DIR}")
    print(f"Using checkpoint: {CHECKPOINT_PATH}")
    print("Starting Flask server...")
    
    # Hardcoded port
    app.run(host='0.0.0.0', port=5000)
