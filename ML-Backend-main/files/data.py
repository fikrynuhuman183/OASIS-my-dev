import pandas as pd
import config
from sklearn.preprocessing import LabelEncoder
from torchvision import  transforms
import torch
from PIL import Image
import os
import cv2
from torch.utils.data import Dataset
import numpy as np
import json
from config import GlobalConfig

config = GlobalConfig()



######################################################################
## FUNCTION DEFINITIONS

def normalize_patientwise(column):
    # Check if the column name is the one to exclude
    ex = ['Age']
    if column.name not in ex :
        return column
    else:
        # Normalize the column
        return (column - column.min()) / (column.max() - column.min())

def normalize_imagewise(column):
    if column.name == 'Clinical presentation of the lesion':
        return (column - column.min()) / (column.max() - column.min())
    else:
        return column
    
def get_image_paths(directory):
    image_paths = []
    groups = []
    valid_image_extensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp']

    # Use os.walk to traverse through all subdirectories
    for foldername, subfolders, filenames in os.walk(directory):
        for filename in filenames:
            # Check if the file has a valid image extension
            if any(filename.lower().endswith(ext) for ext in valid_image_extensions):
                pids = filename.strip().split('/')[-1].strip().split('-')
                # print(pids)
                patient_id = pids[0] + '-' + pids[1]
                groups.append(patient_id)
                # Construct the full path to the image file
                image_path = os.path.join(foldername, filename)
                # Append the image path to the list
                image_paths.append(image_path)
    string_to_num_mapping = {value: index + 1 for index, value in enumerate(set(groups))}

    # Convert the original array to numerical values using the mapping
    groups = [string_to_num_mapping[item] for item in groups]
    return groups, image_paths

def get_image_labels(image_paths):
    image_labels = []
    image_labels_dict = {}
    for path in image_paths:
        label = os.path.basename(os.path.dirname(path))
        if label == 'healthy':
            image_labels_dict[os.path.basename(path)[:-4].split('_')[0]] = 0
        else:
            image_labels_dict[os.path.basename(path)[:-4].split('_')[0]] = 1
        if label == 'oca':
            image_labels.append('opmd')
        else:
            image_labels.append(label)
    label_encoder = LabelEncoder()
    image_labels = label_encoder.fit_transform(image_labels)

    return image_labels, image_labels_dict

#### Data transform and augmentation
data_transform = transforms.Compose([
    transforms.Resize(config.IMAGE_SIZE),
    transforms.ToTensor(),
])

# Define a list of possible augmentations
# augmentations = [
#     transforms.RandomHorizontalFlip(),
#     transforms.RandomVerticalFlip(),
#     transforms.RandomRotation(degrees=(0,270)),
#     transforms.RandomAffine(degrees=0, shear=30),
#     transforms.ColorJitter(brightness=0.2, contrast=0.2, saturation=0.2, hue=0.2),
#     transforms.RandomAdjustSharpness(sharpness_factor=2),
#     transforms.RandomEqualize(),
#     transforms.RandomApply([transforms.GaussianBlur(kernel_size=3)], p=0.5),
#     transforms.RandomApply([], p=0.2),
# ]

# data_aug = transforms.RandomChoice(augmentations)
mean = [0.5123, 0.3595, 0.3125]
std = [0.2664, 0.2174, 0.2110]
def load_and_preprocess_image(image_path, label):
    img = cv2.imread(image_path)
    img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
    img = Image.fromarray(img)
    img = data_transform(img)
    normalize = transforms.Normalize(mean=mean, std=std)
    img = normalize(img)

    fname = os.path.basename(image_path)[:-4]
    image_filename = fname.split("_")[0]
    # print(image_filename)
    metadata = image_patient_metadata.get(image_filename)
    metadata = torch.tensor(metadata, dtype=torch.float32)
    return img, metadata, label

class CustomDataset(Dataset):
    def __init__(self, image_paths , labels, image_patient_metadata):
        self.image_paths = image_paths
        self.labels = labels
        self.image_patient_metadata = image_patient_metadata
        # self.transform = transform


    def __len__(self):
        return len(self.image_paths)

    def __getitem__(self, idx):
        image_path = self.image_paths[idx]
        label = self.labels[idx]
 
        image, metadata, label = load_and_preprocess_image(image_path, label) 
        return image, metadata, label

# End of function definitions
###################################################################
    
image_wise = pd.read_csv(config.image_wise_path)
patient_wise = pd.read_excel(config.patient_wise_path)


# patient_wise['Gender'] = LabelEncoder().fit_transform(patient_wise['Gender'])
# patient_wise['Smoking'] = LabelEncoder().fit_transform(patient_wise['Smoking'])
# patient_wise['Chewing_Betel_Quid '] = LabelEncoder().fit_transform(patient_wise['Chewing_Betel_Quid '])
# patient_wise['Alcohol'] = LabelEncoder().fit_transform(patient_wise['Alcohol'])
# patient_wise = patient_wise.drop(columns=['Image Count'])

columns_to_encode = ['Gender', 'Smoking', 'Chewing_Betel_Quid ', 'Alcohol']

df_encoded = pd.get_dummies(patient_wise, columns=columns_to_encode, dtype=np.int64)

df_encoded.head(3)

patient_wise.drop(columns=['Patient ID', 'Age', 'Severity', 'Image Count', 'Gender', 'Smoking', 'Chewing_Betel_Quid ', 'Alcohol'], inplace=True)
patient_wise = pd.concat([patient_wise, df_encoded], axis=1)
patient_wise.head(2)

normalized_df = patient_wise.apply(normalize_patientwise, axis=0)
image_wise = image_wise.apply(normalize_imagewise, axis=0)



#Load image paths and their groups based on patient id
train_groups, train_image_paths = get_image_paths(config.train_directory)
val_groups, val_image_paths = get_image_paths(config.val_directory)

train_labels, train_labels_dict = get_image_labels(train_image_paths)
val_labels, val_labels_dict = get_image_labels(val_image_paths)

# # get the lesion present from annotations
# json_path = '/home2/oralCancerGroup/OASIS/segmentation/annotationToolDB.images.json'
# annotations = json.load(open(json_path, 'r'))

# lesion_present = {}
# for i in range(len(annotations)):
#     if 'annotation' in annotations[i]:
#         lesion_present[annotations[i]['image_name'][:-4]] = 0
#         for j in range(len(annotations[i]['annotation'])):
#             if annotations[i]['annotation'][j]['name'] == 'Lesion':
#                 lesion_present[annotations[i]['image_name'][:-4]] = 1
#                 break
# image_wise['visible_lesion_present'] = image_wise['Image ID'].map(lesion_present)

all_labels_dict = {**train_labels_dict, **val_labels_dict}

print(f'Number of unique labels: {len(all_labels_dict)}')
print(f'Number of all labels: {len(list(train_labels)) + len(list(val_labels))}')

image_wise.drop(columns=['visible lesion present/ not'], inplace=True)
image_wise['visible_lesion_present'] = image_wise['Image ID'].map(all_labels_dict)

image_wise = image_wise[image_wise['Image ID'].isin(all_labels_dict.keys())]
image_wise['visible_lesion_present'] = image_wise['visible_lesion_present'].astype(np.int64)
image_wise['visible_lesion_present'].fillna(0, inplace=True)


image_wise_encoded = pd.get_dummies(image_wise, columns=['visible_lesion_present'], prefix='visible_lesion', dtype=np.int64)



###Load patient metadata by image ID
image_patient_metadata = {}

for _, row in image_wise_encoded.iterrows():
    pids = row['Image ID'].strip().split('-')
    patient_id = pids[0] + '-' + pids[1]
    
    matching_rows = normalized_df[normalized_df['Patient ID'] == patient_id]

    if not matching_rows.empty:
        image_patient_metadata[row['Image ID']] = [
            matching_rows['Age'].values[0],
            matching_rows['Gender_M'].values[0],
            matching_rows['Gender_F'].values[0],
            matching_rows['Smoking_Yes'].values[0],
            matching_rows['Smoking_No'].values[0],
            matching_rows['Chewing_Betel_Quid _No'].values[0],
            matching_rows['Chewing_Betel_Quid _Yes'].values[0],
            matching_rows['Alcohol_No'].values[0],
            matching_rows['Alcohol_Yes'].values[0],
            # row['visible_lesion_0'],
            # row['visible_lesion_1'],
            # row['Clinical presentation of the lesion']
        ]
    else:
        print(f"No matching rows for patient_id {patient_id}")









