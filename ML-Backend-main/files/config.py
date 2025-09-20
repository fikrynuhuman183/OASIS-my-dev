class GlobalConfig:
    IMAGE_SIZE = (224, 224)
    BATCH_SIZE = 32
    # patient_wise_path = '/storage/scratch1/e17-co328-oral-cavity/segmented_augmented_dataset/Patientwise_Data.xlsx'
    # image_wise_path = '/storage/scratch1/e17-co328-oral-cavity/segmented_augmented_dataset/Imagewise_Data_extra.csv'
    # train_directory = '/storage/scratch1/e17-co328-oral-cavity/segmented_augmented_dataset/train'
    # val_directory = '/storage/scratch1/e17-co328-oral-cavity/segmented_augmented_dataset/val'
    patient_wise_path = '/storage/e17-co328-oral-cavity/segmented_augmented_dataset/Patientwise_Data.xlsx'
    image_wise_path = '/storage/e17-co328-oral-cavity/segmented_augmented_dataset/Imagewise_Data_extra.csv'

    ## Benign only augmented (for original manuscript results)
    # train_directory = '/storage/e17-co328-oral-cavity/opmd_benign_07022024/segmented/train'
    # val_directory = '/storage/e17-co328-oral-cavity/opmd_benign_07022024/segmented/val'

    ## All categories augmented
    # train_directory = '/storage/e17-co328-oral-cavity/opmd_benign_07022024/segmented_bothaugmented/train'
    # val_directory = '/storage/e17-co328-oral-cavity/opmd_benign_07022024/segmented_bothaugmented/val'

    # ## Original augmentation
    # train_directory = '/storage/e17-co328-oral-cavity/opmd_benign_07022024/segmented/train'
    # val_directory = '/storage/e17-co328-oral-cavity/opmd_benign_07022024/segmented/val'

    ## Unaugmented
    train_directory = '/storage/e17-co328-oral-cavity/opmd_benign_07022024/segmented_unaugmented/train'
    val_directory = '/storage/e17-co328-oral-cavity/opmd_benign_07022024/segmented_unaugmented/val'
    # mask_directory = '/home2/oralCancerGroup/dataset_07_07_2023/newCategories/augmented_masks/train'
    NUM_CLASSES = 2
    

    # custom_encoding = {0: 'opmd', 1: 'benign', 2: 'healthy', 3: 'nan'}
    # custom_decoding = {'opmd': 0, 'benign' : 1,'healthy': 2, 'oca': 0}

    # vit_config = {
    #     'input_shape': (3, 224, 224),
    #     'patch_size': 16,
    #     'num_patches': 768,
    #     'projection_dim': 3 * 16 * 16,
    #     'transformer_layers': 12,
    #     'num_heads': 8,
    #     'transformer_units': 512,
    #     'mlp_head_units': [512, 256],
    #     'num_classes': NUM_CLASSES,
    # }
