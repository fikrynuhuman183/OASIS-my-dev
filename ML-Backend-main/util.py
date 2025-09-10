from dotenv import load_dotenv
import os
import pandas as pd
from PIL import Image
import tensorflow as tf
import numpy as np
from sklearn.preprocessing import LabelEncoder
import pickle

img_height = 224
img_width = 224

load_dotenv()

def findEntry(imageName: str):
    csvPath = os.getenv('CSV_PATH')
    data_df = pd.read_csv(csvPath)
    entry = data_df.loc[data_df['Image ID']==imageName]
    return entry

def nullRemoval(entry:pd.DataFrame):

    new_entry =  {'Gender': 'Male', 'Smoking':'yes', 'Chewing Betel Quid ': 'yes', 'Alcohol': 'yes'}
    new_entry1 = {'Gender': 'pns', 'Smoking':'ex', 'Chewing Betel Quid ': 'ex', 'Alcohol': 'occational'}
    new_entry2 = {'Gender': 'Female', 'Smoking':'no', 'Chewing Betel Quid ': 'no', 'Alcohol': 'no'}
    new_entry3 = {'Gender': 'Male', 'Smoking':'occational', 'Chewing Betel Quid ': 'occational', 'Alcohol': 'yes'}


    new_data = pd.DataFrame([new_entry, new_entry1, new_entry2, new_entry3])
    entry = pd.concat([entry, new_data], ignore_index=True)
    entry['Age'].fillna(value='35', inplace=True)
    entry["Smoking"] = entry["Smoking"].fillna("No")
    entry["Chewing Betel Quid "] = entry["Chewing Betel Quid "].fillna("No")
    entry["Alcohol"] = entry["Alcohol"].fillna("No")
    entry["Clinical diagnosis "] = entry["Clinical diagnosis "].fillna("None")
    entry["Clinical presentation of the lesion"] = entry["Clinical presentation of the lesion"].fillna("None")
    entry["Medical history"] = entry["Medical history"].fillna("NA")
    entry["History of presenting complaint"] = entry["History of presenting complaint"].fillna("No Data")
    entry["Gender"] = entry["Gender"].fillna("PNS")
    return entry


def create_list_data(list_):
  list_ = list_.replace("+", ",")
  list_ = list_.replace("/", ",")
  list_ = list_.replace("?", "")
  return list_.split(",")


def dataCleaning(entry:pd.DataFrame):
    entry = entry.drop(['Image ID', 'New Image ID', 'Pt Registration no', 'Folder no', 'Image captured by',
                         'Other investigations/tests', 'Comments', 'location of the mouth'
, 'Chief complaint', 'Histopathological disgnosis', 'Medication history', 'Duration of practising habits, if any',
'Oral hygiene product used', 'visible lesion present/ not'], axis=1)
    str_columns = ['Gender', 'Smoking', 'Chewing Betel Quid ', 'Alcohol',
       'Clinical diagnosis ', 'Clinical presentation of the lesion',
       'History of presenting complaint', 'Medical history',
       'Image Category (OCA/ OPMD/ Benign/ Healthy)']
    for i in str_columns:
        entry[i] = entry[i].str.lower()
    entry['Smoking'] = entry['Smoking'].str.strip()
    entry['Chewing Betel Quid '] = entry['Chewing Betel Quid '].str.strip()

    entry['Gender'] = entry['Gender'].replace('f', 'female')
    entry['Gender'] = entry['Gender'].replace('m', 'male')
    entry['Gender'] = entry['Gender'].replace('female ', 'female')
    entry['Gender'] = entry['Gender'].replace('male ', 'male')
    entry['Smoking'] = entry['Smoking'].replace([
        'yes-beedi', 'yes; 01/day', 'yes; 3 beedi for 2 years'
        ,'yes;cigarets/bidi 2-3 for 50 yrs', 'beedi smoker', 'chronic smoker', 'yes, bidi', 'yes;5 bidi/day'], 'yes')
    entry['Smoking'] = entry['Smoking'].replace(['occasional', 'occcational'], 'occational')
    entry['Smoking'] = entry['Smoking'].replace(['none'], 'no')
    entry['Smoking'] = entry['Smoking'].replace(['ex smoker', 'ex user - beedi'], 'ex')

    entry['Chewing Betel Quid '] = entry['Chewing Betel Quid '].replace(['occasional', 'occcational', 'yes(occational)'], 'occational')
    entry['Chewing Betel Quid '] = entry['Chewing Betel Quid '].replace(['ex user', 'ex chewer'], 'ex')
    entry['Chewing Betel Quid '] = entry['Chewing Betel Quid '].replace(['yes + mawa', 'yes; 2-3 quids per day for 15-20 yrs', 'yes; 4-5quids/day 2 years duration',
                                                                            'yes; >10quids/day for >15yrs', 'yes; >5quids/day with all 4 ingredients', 'clove nut chewer',
                                                                            'thul (since 2012)and mawa', 'mawa', 'arecanut chewing?', 'arecanut', 'for a long time, more than 50 years',
       'none', 'yes; >25quids/day for >50yrs', 'yes;6 quids/day',], 'yes')
    
    entry['Alcohol'] = entry['Alcohol'].replace(['yes; occational', 'yes;occasionally'], 'occational')
    entry['Alcohol'] = entry['Alcohol'].replace(['yes;1/4 bottle/day'], 'yes')
    entry['Alcohol'] = entry['Alcohol'].replace(['none'], 'no')

    entry['Clinical diagnosis '] = entry['Clinical diagnosis '].str.strip()
    entry['Clinical diagnosis '] = entry['Clinical diagnosis '].str.upper()

    entry['Clinical diagnosis '] = entry['Clinical diagnosis '].apply(create_list_data)
    entry['Clinical diagnosis '] = entry['Clinical diagnosis ']
    entry['Clinical diagnosis '] = entry['Clinical diagnosis '].apply(lambda x:x[0])

    entry['Age'] = entry['Age'].map(lambda x: x.lstrip('`').rstrip('`'))

    entry.drop([
       'Clinical presentation of the lesion',
       'History of presenting complaint', 'Medical history'], axis=1, inplace=True)
    
    entry['Age'] = entry['Age'].astype(str).astype(int)

    return entry

def dataProcessing(entry):
    dummy_features = ['Gender', 'Smoking', 'Chewing Betel Quid ', 'Alcohol']
    
    arr = np.load(os.getenv('CLINICAL'), allow_pickle=True)['arr_0']
    category_to_label = {category: label for label, category in enumerate(arr)}
    entry['Clinical diagnosis '] = entry['Clinical diagnosis '].apply(lambda x: category_to_label[x])


    X = pd.get_dummies(entry, columns=dummy_features)
    X.drop(['Image Category (OCA/ OPMD/ Benign/ Healthy)'], axis=1, inplace=True)
    return X

def metaPredict(entry):
    null_rem = nullRemoval(entry)
    cleaned = dataCleaning(null_rem)
    x = dataProcessing(cleaned)
    head = x.head(1)
    
    with open(os.getenv('PKL_PATH'), 'rb') as file:
        xgb_model = pickle.load(file)
    values = head.values


    prediction = xgb_model.predict(values, output_margin=True)
    return prediction

def calculate_total(imagePred, metaPred):
    meta_prob = np.exp(metaPred) / np.exp(metaPred).sum()
    oca, opmd, benign, healthy = meta_prob.tolist()[0]
    meta_prob_3 = np.array([benign, healthy, oca+opmd])
    return [meta_prob_3*0.4 + imagePred[0]*0.6]

    


def nnPredict(image_path:str):
    model = tf.keras.models.load_model(os.getenv('H5_FILE_PATH'))
    img = Image.open(image_path)
    img = img.resize((img_height, img_width))
    img_array = np.reshape(img, (1, img_height, img_width, 3))
    pred = model.predict(img_array)
    return pred
