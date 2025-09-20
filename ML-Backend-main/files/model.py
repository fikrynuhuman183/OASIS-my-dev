import torch
import torch.nn as nn
from torchvision import models, transforms
import timm
from timm.models.layers import SelectAdaptivePool2d
import pickle
import numpy as np

# class LeNet(nn.Module):
    
#     def __init__(self):
#         super(LeNet, self).__init__()
#         self.feature_extractor = nn.Sequential(
#             nn.Conv2d(in_channels=3, out_channels=6, kernel_size=5, stride=1, padding=0),
#             nn.ReLU(),
#             nn.AvgPool2d(kernel_size=2, stride=2),

#             nn.Conv2d(in_channels=6, out_channels=16, kernel_size=5, stride=1, padding=0),
#             nn.ReLU(),
#             nn.AvgPool2d(kernel_size=2, stride=2),
#         )

#         self.classifier = nn.Sequential(
#             nn.Linear(16 * 53 * 53, 120),  # in_features = 16 x 53 x 53
#             nn.ReLU(),
#             nn.Linear(120, 84),
#             nn.ReLU(),
#             nn.Linear(84, 3),
#             nn.Softmax()
#         )
        
#     def forward(self, x): 
#         a1 = self.feature_extractor(x)
#         a1 = torch.flatten(a1, 1)
#         a2 = self.classifier(a1)
#         return a2

# Early fusion 2
class CustomModel(nn.Module):
    def __init__(self, num_classes, model_name, metadata, features):
        print(features)
        super(CustomModel, self).__init__()

        self.base_model = timm.create_model(model_name, pretrained=True, num_classes=0)
        # self.freeze_module(self.base_model)

        print(self.base_model.eval())
        if model_name in ["tf_mobilenetv3_small_075", "tf_mobilenetv3_large_075"]:
            self.base_model.conv_head = nn.Identity()
            self.base_model.act2 = nn.Identity()
            self.base_model.flatten = nn.Identity()

        xgboost_model_path = "/home2/oralCancerGroup/OASIS/mask_metadata_dinura/model/model_xgb/isuris_experiments/opmd_benign/model/"+metadata+".pkl"

        with open(xgboost_model_path, 'rb') as f:
            self.metadata_feature_extractor = pickle.load(f)   

        self.image_feature_extractor = nn.Sequential(
            nn.Dropout1d(p=0.1),    
        )
        self.fc = nn.Sequential(
            nn.Linear(features, 360),
            nn.GELU(),
            nn.Linear(360, 180),
            nn.GELU(),
            nn.Linear(180, 128),
            nn.GELU(),
        )

        self.fc = nn.Sequential(
            nn.Linear(features+32, 360),
            nn.GELU(),
            nn.Linear(360, 180),
            nn.GELU(),
            nn.Linear(180, 128),
            nn.GELU(),
        )

        self.metadata_2 = nn.Sequential(
            nn.Linear(2,32),
            nn.ReLU(inplace=True),
            nn.Dropout1d(p=0.1),
        )


        self.output_layer = nn.Sequential(
            nn.Linear(128, num_classes),
            # nn.Softmax(dim=1),
            nn.Sigmoid(),
        )

    # def freeze_module(self, module):
    #     for param in module.parameters():
    #         param.requires_grad = False

    def forward(self, image, metadata):
        image_features = self.base_model(image)
        image_features = image_features.view(image_features.size(0), -1) 
        image_features = self.image_feature_extractor(image_features)
        image_features = image_features.view(image_features.size(0), -1) 
        # image_features = self.fc(image_features)
        metadata_np = metadata.detach().cpu().numpy()
        metadata_features = self.metadata_feature_extractor.predict_proba(metadata_np)
        metadata_features = torch.from_numpy(metadata_features.astype(np.float32)).to('cuda')
        metadata_features = self.metadata_2(metadata_features)
        concat_features = torch.cat((image_features, metadata_features), dim=1)
        concat_features = self.fc(concat_features)
        predictions = self.output_layer(concat_features)
        return predictions


## Early Fusion
# class CustomModel(nn.Module):
#     def __init__(self, num_classes, model_name, metadata, features):
#         print(features)
#         super(CustomModel, self).__init__()

#         self.base_model = timm.create_model(model_name, pretrained=True, num_classes=0)
#         # self.freeze_module(self.base_model)

#         print(self.base_model.eval())
#         if model_name in ["tf_mobilenetv3_small_075", "tf_mobilenetv3_large_075"]:
#             self.base_model.conv_head = nn.Identity()
#             self.base_model.act2 = nn.Identity()
#             self.base_model.flatten = nn.Identity()

#         # xgboost_model_path = "/home2/oralCancerGroup/OASIS/mask_metadata_dinura/model/model_xgb/isuris_experiments/opmd_benign/model/"+metadata+".pkl"

#         # with open(xgboost_model_path, 'rb') as f:
#         #     self.metadata_feature_extractor = pickle.load(f)   

#         self.image_feature_extractor = nn.Sequential(
#             nn.Dropout1d(p=0.1),    
#         )
#         self.fc = nn.Sequential(
#             nn.Linear(features, 360),
#             nn.GELU(),
#             nn.Linear(360, 180),
#             nn.GELU(),
#             nn.Linear(180, 128),
#             nn.GELU(),
#         )
#         self.metadata_2 = nn.Sequential(
#             nn.Linear(2,32),
#             nn.ReLU(inplace=True),
#             nn.Dropout1d(p=0.1),
#         )


#         self.output_layer = nn.Sequential(
#             nn.Linear(128, num_classes),
#             # nn.Softmax(dim=1),
#             nn.Sigmoid(),
#         )

#     # def freeze_module(self, module):
#     #     for param in module.parameters():
#     #         param.requires_grad = False

#     def forward(self, image, metadata):
#         # processing metadata features
#         batch_size = image.shape[0]
#         # print(f"batch size: {batch_size}")
#         # print(f"metadata shape: {metadata.shape}")
#         # print(f"image shape: {image.shape}")
#         num_repeats = (224 * 224) // 9 + 1
#         reshaped_metadata_list = []
#         for i in range(batch_size):
#             # Repeat the metadata tensor for the current batch element
#             repeated_metadata = metadata[i].repeat(num_repeats)
            
#             # Truncate the repeated metadata to the required number of elements
#             truncated_metadata = repeated_metadata[:224 * 224]
            
#             # Reshape the tensor to (1, 224, 224) for the current batch element
#             reshaped_metadata = truncated_metadata.view(1, 224, 224)
            
#             # Append to the list
#             reshaped_metadata_list.append(reshaped_metadata)

#         # Stack the reshaped metadata tensors to form a batch (batch_size, 1, 224, 224)
#         reshaped_metadata_batch = torch.stack(reshaped_metadata_list)
#         # print(f"repeated_metadata shape: {reshaped_metadata_batch.shape}")
#         truncated_metadata = reshaped_metadata_batch[:, :224 * 224]
#         # print(f"truncated_metadata shape: {truncated_metadata.shape}")
#         reshaped_metadata = truncated_metadata.view(batch_size, 1, 224, 224)
#         image = torch.cat((image[:, :2, :, :], reshaped_metadata), dim=1)

#         image_features = self.base_model(image)
#         image_features = image_features.view(image_features.size(0), -1) 
#         image_features = self.image_feature_extractor(image_features)
#         image_features = image_features.view(image_features.size(0), -1) 
#         image_features = self.fc(image_features)
#         # concat_features = torch.cat((image_features, metadata_features), dim=1)
#         predictions = self.output_layer(image_features)
#         return predictions


# class CustomModel(nn.Module):
#     def __init__(self, num_classes, model_name, metadata, features):
#         print(features)
#         super(CustomModel, self).__init__()

#         self.base_model = timm.create_model(model_name, pretrained=True, num_classes=0)
#         # self.freeze_module(self.base_model)

#         print(self.base_model.eval())
#         if model_name in ["tf_mobilenetv3_small_075", "tf_mobilenetv3_large_075"]:
#             self.base_model.conv_head = nn.Identity()
#             self.base_model.act2 = nn.Identity()
#             self.base_model.flatten = nn.Identity()

#         xgboost_model_path = "/home2/oralCancerGroup/OASIS/mask_metadata_dinura/model/model_xgb/isuris_experiments/opmd_benign/model/"+metadata+".pkl"

#         with open(xgboost_model_path, 'rb') as f:
#             self.metadata_feature_extractor = pickle.load(f)   

#         self.image_feature_extractor = nn.Sequential(
#             nn.Dropout1d(p=0.1),    
#         )
#         self.fc = nn.Sequential(
#             nn.Linear(features, 360),
#             nn.GELU(),
#             nn.Linear(360, 180),
#             nn.GELU(),
#             nn.Linear(180, 128),
#             nn.GELU(),
#         )
#         self.metadata_2 = nn.Sequential(
#             nn.Linear(2,32),
#             nn.ReLU(inplace=True),
#             nn.Dropout1d(p=0.1),
#         )


#         self.output_layer = nn.Sequential(
#             nn.Linear(128+32, num_classes),
#             # nn.Softmax(dim=1),
#             nn.Sigmoid(),
#         )

#     # def freeze_module(self, module):
#     #     for param in module.parameters():
#     #         param.requires_grad = False

#     def forward(self, image, metadata):
#         image_features = self.base_model(image)
#         image_features = image_features.view(image_features.size(0), -1) 
#         image_features = self.image_feature_extractor(image_features)
#         image_features = image_features.view(image_features.size(0), -1) 
#         image_features = self.fc(image_features)
#         metadata_np = metadata.detach().cpu().numpy()
#         metadata_features = self.metadata_feature_extractor.predict_proba(metadata_np)
#         metadata_features = torch.from_numpy(metadata_features.astype(np.float32)).to('cuda')
#         metadata_features = self.metadata_2(metadata_features)
#         concat_features = torch.cat((image_features, metadata_features), dim=1)
#         predictions = self.output_layer(concat_features)
#         return predictions

#gihan best 
# class CustomModel(nn.Module):
#     def __init__(self, num_classes, model_name, metadata):
        
#         super(CustomModel, self).__init__()

#         self.base_model = timm.create_model(model_name, pretrained=True, num_classes=0)
#         # self.base_model.conv_head = nn.Identity()
#         # self.base_model.act2 = nn.Identity()
#         # self.base_model.flatten = nn.Identity()

#         xgboost_model_path = "/home2/oralCancerGroup/OASIS/mask_metadata_dinura/model/model_xgb/isuris_experiments/opmd_benign/model/"+metadata+".pkl"

#         with open(xgboost_model_path, 'rb') as f:
#             self.metadata_feature_extractor = pickle.load(f)   

#         self.image_feature_extractor = nn.Sequential(
#             nn.Dropout1d(p=0.1),    
#         )
#         self.fc = nn.Sequential(
#             nn.Linear(1536, 360),
#             nn.GELU(),
#             nn.Linear(360, 180),
#             nn.GELU(),
#             nn.Linear(180, 128),
#             nn.GELU(),
#         )
#         self.metadata_2 = nn.Sequential(
#             nn.Linear(2,32),
#             nn.ReLU(inplace=True),
#             nn.Dropout1d(p=0.1),
#         )


#         self.output_layer = nn.Sequential(
#             nn.Linear(128+32, num_classes),
#             nn.Softmax(dim=1),
#         )

#     def forward(self, image, metadata):
#         image_features = self.base_model(image)
#         image_features = image_features.view(image_features.size(0), -1) 
#         image_features = self.image_feature_extractor(image_features)
#         image_features = image_features.view(image_features.size(0), -1) 
#         image_features = self.fc(image_features)
#         metadata_np = metadata.detach().cpu().numpy()
#         metadata_features = self.metadata_feature_extractor.predict_proba(metadata_np)
#         metadata_features = torch.from_numpy(metadata_features.astype(np.float32)).to('cuda')
#         metadata_features = self.metadata_2(metadata_features)
#         concat_features = torch.cat((image_features, metadata_features), dim=1)
#         predictions = self.output_layer(concat_features)
#         return predictions
# class CustomModel(nn.Module):
#     def __init__(self, num_classes, model_name, input_features):
#         super(CustomModel, self).__init__()
#         self.base_model = timm.create_model(model_name, pretrained=True,num_classes=128)
#         self.image_feature_extractor = nn.Sequential(
#         )

#         self.metadata_feature_extractor = nn.Sequential(
#             nn.Linear(11, 32),
#             nn.GELU(),
#             nn.Linear(32, 128),
#             nn.GELU(),
#         )

#         self.attention = nn.Sequential(
#             nn.Linear(256, 192),
#             nn.Tanh(),
#             nn.Linear(192, 1),
#             nn.Softmax(dim=1),
#         )

#         self.fc = nn.Sequential(
#             nn.Linear(256, 64),
#             nn.GELU(),
#             nn.Linear(64, 32),
#             nn.GELU(),
#         )

#         self.output_layer = nn.Sequential(
#             nn.Linear(32, num_classes),
#             nn.Softmax(dim=1),
#         )

#     def forward(self, image, metadata):
#         image_features = self.base_model(image)
#         image_features = self.image_feature_extractor(image_features)
#         metadata_features = self.metadata_feature_extractor(metadata)
#         image_features = image_features.view(image_features.size(0), -1)

#         combined_features = torch.cat((image_features, metadata_features), dim=1)


#         attention_weights = self.attention(combined_features)
#         attended_features = attention_weights * combined_features


#         combined_features = self.fc(attended_features)

#         predictions = self.output_layer(combined_features)
#         return predictions

# class LeNet(nn.Module):
    
#     def __init__(self):
#         super(LeNet, self).__init__()
#         self.feature_extractor = nn.Sequential(
#             nn.Conv2d(in_channels=3, out_channels=6, kernel_size=5, stride=1, padding=0),
#             nn.ReLU(),
#             nn.AvgPool2d(kernel_size=2, stride=2),

#             nn.Conv2d(in_channels=6, out_channels=16, kernel_size=5, stride=1, padding=0),
#             nn.ReLU(),
#             nn.AvgPool2d(kernel_size=2, stride=2),
#         )

#         self.classifier = nn.Sequential(
#             nn.Linear(16 * 53 * 53, 120),  # in_features = 16 x 53 x 53
#             nn.ReLU(),
#             nn.Linear(120, 84),
#             nn.ReLU(),
#             nn.Linear(84, 128),
#             nn.Softmax()
#         )
        
#     def forward(self, x): 
#         a1 = self.feature_extractor(x)
#         a1 = torch.flatten(a1, 1)
#         a2 = self.classifier(a1)
#         return a2
    
    
# class CustomModel(nn.Module):
#     def __init__(self, num_classes, model_name, input_features):
        
#         super(CustomModel, self).__init__()

#         # # Pre-trained model (DenseNet121 in this example)121, 201
#         # self.base_model = timm.create_model(model_name, pretrained=True)
#         # self.base_model = timm.create_model(model_name, pretrained=True,num_classes=128)
#         self.base_model = LeNet()
        
#         # num_ftrs = self.base_model.classifier.out_features
#         # print(num_ftrs)
#         # self.base_model.classifier = nn.Identity()
#         # print(self.base_model.shape)
# #arch 1: val> train combines 128 features each
# #arch 2: val< train combines 128 features each lr=0.0001
# #arch 3: combines 64 features each
#         # self.image_feature_extractor = nn.Sequential(
#         #     # #arch 1:
#         #     # nn.Linear(2208, 1024),
#         #     # nn.Linear(1024, 256),
#         #     # nn.ReLU(inplace=True),
#         #     # nn.Linear(256, 128),
#         #     # nn.Dropout1d(p=0.5),

#         #     # #arch 2: 
#         #     # nn.Linear(2048, 1024),
#         #     # nn.ReLU(inplace=True),
#         #     # nn.Linear(1024, 128),
#         #     # nn.Dropout1d(p=0.1),

#         #     #arch 3:
#         #     # nn.Linear(input_features, 1024),
#         #     # nn.ReLU(inplace=True),
#         #     # nn.Dropout1d(p=0.2),
#         #     # nn.Linear(1024, 64),
#         #     # nn.ReLU(inplace=True),
#         #     # nn.Dropout1d(p=0.2),

#         #     # #arch 4:
#         #     # nn.Linear(input_features, 1024),
#         #     # nn.ReLU(inplace=True),
#         #     # nn.Linear(1024, 128),
#         #     # nn.ReLU(inplace=True),   
#         #     # #arch 5:
#         #     # nn.Linear(input_features, 1024),
#         #     # nn.ReLU(inplace=True),
#         #     # nn.Dropout1d(p=0.2),
#         #     # nn.Linear(1024, 128),
#         #     # nn.ReLU(inplace=True), 
#         #     # #arch 7:
#         #     # nn.Dropout1d(p=0.2),  
#         #     #arch 8:
#         #     nn.Dropout1d(p=0.1),    



#         # )

#         self.metadata_feature_extractor = nn.Sequential(
#             # #arch 1:
#             # nn.Linear(7, 128),
#             # nn.ReLU(inplace=True),
#             # nn.Linear(128, 128),
#             # nn.ReLU(inplace=True),
#             # nn.Dropout1d(p=0.2),

#             # # arch 2:
#             # nn.Linear(7, 128),
#             # nn.ReLU(inplace=True),
#             # # nn.Linear(128, 64),
#             # # nn.ReLU(inplace=True),
#             # nn.Dropout1d(p=0.2),

#             # #arch 3:
#             # nn.Linear(7, 64),
#             # nn.ReLU(inplace=True),
#             # nn.Dropout1d(p=0.2),
#             #  #arch 4:
#             # nn.Linear(7, 64),
#             # nn.ReLU(inplace=True),
#             #arch 5:
#             nn.Linear(12, 64),
#             nn.GELU(),
#             nn.Linear(64, 64),
#             nn.GELU(),

#         )

#         self.fc = nn.Sequential(
#             # arch 1:
#             # nn.Linear(256, 128),
#             # nn.ReLU(),
#             # nn.Dropout1d(p=0.2),
#             # nn.Linear(128, 64),
#             # nn.ReLU(),
#             # nn.Dropout1d(p=0.2),
#             # nn.Linear(64, 32),
#             # nn.ReLU(),

#             # #arch 2
#             # nn.Linear(256, 32),
#             # nn.ReLU(),
#             # nn.Dropout1d(p=0.2),

#             #arch3:
#             # nn.Linear(128, 64),
#             # nn.ReLU(),
#             # nn.Linear(64, 32),
#             # nn.ReLU(),
#             # nn.Dropout1d(p=0.2)

#             # #arch4:
#             # nn.Linear(192, 64),
#             # nn.ReLU(),
#             # nn.Linear(64, 32),
#             # nn.ReLU(),

#             #arch5:
#             nn.Linear(192, 64),
#             # nn.Dropout1d(p=0.2),
#             nn.GELU(),
#             nn.Linear(64, 32),
#             nn.GELU(),
            
#          )

#         # Output layer
#         self.output_layer = nn.Sequential(
#             nn.Linear(32, num_classes),
#             nn.Softmax(dim=1),
#         )

    # def forward(self, image, metadata):
    #     image_features = self.base_model(image)
    #     # image_features = self.image_feature_extractor(image_features)
    #     metadata_features = self.metadata_feature_extractor(metadata)
        
    #     # Flatten the image features
    #     image_features = image_features.view(image_features.size(0), -1) # torch.Size([16, 1024])
        
    #     # Concatenate image and metadata features
    #     combined_features = torch.cat((image_features, metadata_features), dim=1) # size [16,1152]
        
    #     # Pass through fully connected layers
    #     combined_features = self.fc(combined_features)
        
    #     # Pass through the output layer
    #     predictions = self.output_layer(combined_features)

        
    #     return predictions
    

# #here's to making stupid changes 🍻
# class CustomModel(nn.Module):
#     def __init__(self, num_classes, model_name, input_features):
        
#         super(CustomModel, self).__init__()

#         # # Pre-trained model (DenseNet121 in this example)121, 201
#         # self.base_model = timm.create_model(model_name, pretrained=True)
#         self.base_model = timm.create_model(model_name, pretrained=True,num_classes=32)
        
#         self.image_feature_extractor = nn.Sequential(   
#             nn.Dropout1d(p=0.1),

#         )

#         self.metadata_feature_extractor = nn.Sequential(
#             nn.Linear(7, 64),
#             nn.ReLU(inplace=True),

#         )

#         self.fc = nn.Sequential(
#             #arch5:
#             nn.Linear(96, 32),
#             nn.ReLU(),
           
#          )

#         # Output layer
#         self.output_layer = nn.Linear(32, num_classes)

#     def forward(self, image, metadata):
#         image_features = self.base_model(image)
#         image_features = self.image_feature_extractor(image_features)
#         metadata_features = self.metadata_feature_extractor(metadata)
        
#         # Flatten the image features
#         image_features = image_features.view(image_features.size(0), -1) # torch.Size([16, 1024])
        
#         # Concatenate image and metadata features
#         combined_features = torch.cat((image_features, metadata_features), dim=1) # size [16,1152]
        
#         # Pass through fully connected layers
#         combined_features = self.fc(combined_features)
        
#         # Pass through the output layer
#         predictions = self.output_layer(combined_features)

        
#         return predictions
    
