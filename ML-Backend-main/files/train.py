import torch
import pytorch_lightning as pl
import argparse
import torch.nn as nn

from torch.utils.data import Dataset, DataLoader

from data import CustomDataset
from config import GlobalConfig
from data import *
from model import CustomModel
from pytorch_lightning.callbacks import ModelCheckpoint
from sklearn.utils.class_weight import compute_class_weight
import torch.nn.functional as F
from torch.optim.lr_scheduler import CosineAnnealingLR, MultiStepLR, LambdaLR, StepLR


import datetime
from pytorch_lightning.loggers import TensorBoardLogger#, WandbLogger

class CustomLightningModule(pl.LightningModule):
    def __init__(self, model, criterion, train_dataloader):
        super(CustomLightningModule, self).__init__()
        self.model = model
        self.criterion = criterion
        # self.optimizer = optimizer
        # self.scheduler = scheduler
        self.learning_rate = 0.001
        self.weight_decay = 1e-5 #0.0001
        self.momentum = 0.9
        self.lr_decay_steps = [15, 30, 45]
        self.lr_decay_factor = 0.3
        self.warmup_epochs = 5
        self.train_dataloader = train_dataloader

    def forward(self, x,metadata):
        return self.model(x,metadata)

    def training_step(self, batch, batch_idx):
        images, metadata, labels = batch
        labels = F.one_hot(labels, num_classes=2).float()
        outputs = self(images, metadata)
        loss = self.criterion(outputs, labels)
        predicted = torch.argmax(outputs, 1)
        accuracy = (predicted == torch.argmax(labels, dim=1)).float().mean()

        self.log('train_accuracy',accuracy, on_epoch=True, prog_bar=True)
        self.log('train_loss', loss)
        return loss

    def validation_step(self, batch, batch_idx):
        images, metadata, labels = batch
        labels = F.one_hot(labels, num_classes=2).float()
        outputs = self(images, metadata)
        loss = self.criterion(outputs, labels)
        predicted = torch.argmax(outputs, 1)
        accuracy = (predicted == torch.argmax(labels, dim=1)).float().mean()
        self.log('val_loss', loss)
        # self.log('val_accuracy', accuracy)
        # val accuracy epoch
        self.log('val_accuracy',accuracy, on_epoch=True, prog_bar=True)
        return accuracy

    def configure_optimizers(self):
        # optimizer = torch.optim.Adam(model.parameters(), lr=args.lr, weight_decay=1e-5)
        # optimizer = torch.optim.SGD(self.model.parameters(), lr=self.learning_rate,
        #                       weight_decay=self.weight_decay, momentum=self.momentum, nesterov=True)
        optimizer = torch.optim.AdamW(self.model.parameters(), lr=self.learning_rate, weight_decay=self.weight_decay, amsgrad=True)
        # scheduler = MultiStepLR(optimizer, milestones=self.lr_decay_steps, gamma=self.lr_decay_factor)
        # scheduler = CosineAnnealingLR(optimizer, T_max=10, eta_min=0.0001)
        # return [optimizer], [scheduler]
        return optimizer

    # def configure_optimizers(self):
    #     optimizer = torch.optim.AdamW(self.model.parameters(), lr=self.learning_rate, weight_decay=self.weight_decay)
    #     warmup_iters = len(self.train_dataloader) * self.warmup_epochs  # Adjust warmup_epochs
    #     warmup_schedule = {
    #         'scheduler': LambdaLR(optimizer, lr_lambda=lambda epoch: epoch / warmup_iters),
    #         'interval': 'step',  # Apply warmup at every step
    #     }
    #     decay_schedule = {
    #         'scheduler': StepLR(optimizer, step_size=20 // 2, gamma=self.lr_decay_factor),
    #         'interval': 'epoch',  # Apply decay after warmup
    #     }
    #     return [optimizer], [warmup_schedule, decay_schedule]


   

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Train a model')
    config = GlobalConfig()

    parser.add_argument('--lr', type=float, default=0.001, help='learning rate')
    parser.add_argument('--batch_size', type=int, default=32, help='batch size')
    parser.add_argument('--epochs', type=int, default=50, help='number of epochs')
    parser.add_argument('--num_workers', type=int, default=32, help='number of workers')
    parser.add_argument('--loadfromcheckpoint', type=int, default=-1, help='load from checkpoint')
    parser.add_argument('--logdir', type=str, default='logs_augmented_data', help='log directory')
    parser.add_argument('--model_name', type=str, default='hrnet_w18', help='hrnet_w18,inception_v3, resnet50,densenet121')
    parser.add_argument('--metadata', type=str, default=True, help='metadata')
    parser.add_argument('--pref', type=str, default=True, help='prefix')
    args = parser.parse_args()

    input_features = {'hrnet_w18':2048, 'inception_v3': 2048, 'resnet50': 2048, 'densenet121': 1024, 'mixnet_s': 1536, 'tf_mobilenetv3_large_075': 720, 'tf_mobilenetv3_small_075':432, 'tf_efficientnet_lite0':1000, 'Lenet':10, 'mobilenetv2_035':1000, 'tf_efficientnet_b0':1280, 'mobilenetv2_050' : 1000, 'Alexnet':1000, 'mobilenetv3_small_050': 1000 }
    train_dataset = CustomDataset(train_image_paths, train_labels, image_patient_metadata)
    val_dataset = CustomDataset(val_image_paths, val_labels, image_patient_metadata)
    # num_classes, model_name, input_features
    ### model_name = 'hrnet_w18', 'inception_v3', 'resnet50', 'densenet121'
 
    class_weights = compute_class_weight('balanced', classes=np.unique(train_labels), y=train_labels)
    print(input_features[args.model_name])
    model = CustomModel(config.NUM_CLASSES, args.model_name, args.metadata, input_features[args.model_name])
    # criterion = nn.CrossEntropyLoss(weight=torch.tensor(class_weights).float())
    criterion = nn.BCEWithLogitsLoss(pos_weight=torch.tensor(class_weights).float())
    # criterion = nn.BCELoss(weight=torch.tensor(class_weights).float())
    # optimizer = torch.optim.Adam(model.parameters(), lr=args.lr, weight_decay=1e-5)
    # scheduler = CosineAnnealingLR(optimizer, T_max=10, eta_min=0.0001)
    train_loader = DataLoader(train_dataset, batch_size=args.batch_size, shuffle=True, num_workers=args.num_workers, persistent_workers=True)
    val_loader = DataLoader(val_dataset, batch_size=args.batch_size, num_workers=args.num_workers, persistent_workers=True)
    lightning_module = CustomLightningModule(model, criterion, train_loader)

    current_datetime = datetime.datetime.now()
    experiment_name = f'{args.pref}_{args.model_name}_{args.lr}_{args.epochs}_{current_datetime.strftime("%Y-%m-%d_%H-%M-%S")}'
    # experiment_name = args.logdir + '/'+experiment_name
    # experiment_name = "torch_exp_dinura_vit_large_patch16_224_ptEncoding_with_masks_O_M=7_" + experiment_name
    # experiment_name = f"gihan_arch_with_metadata_epoch_200_bce_loss_sigmoid_xgb_model/tf_mobilenetv3_large_075_tf_mobilenetv3_large_075_0.001_200_2024-04-14_19-44-48"
    tensorboard_logger = TensorBoardLogger(save_dir=args.logdir + '/gihan_arch', name=experiment_name)

    # wandb_logger = WandbLogger(project='Oralcancer', entity='OASIS', name=experiment_name)

    checkpoint_callback = ModelCheckpoint(save_weights_only=False, mode="max", monitor="val_accuracy", save_top_k=10, save_last=True,
                                      dirpath='Final_20240415_BCE/'+experiment_name, filename="best_{epoch:02d}-{val_accuracy:.3f}-{train_accuracy:.3f}-{val_loss:.3f}-{train_loss:.3f}")
    
    checkpoint_callback.CHECKPOINT_NAME_LAST = "{epoch}-last" # type: ignore

    loadFromCheckPoint = args.loadfromcheckpoint



    if loadFromCheckPoint>0:
        trainer = pl.Trainer(max_epochs=args.epochs,
                        accelerator="gpu", devices=[0],
                        logger=[tensorboard_logger],#, wandb_logger],
                        callbacks=[checkpoint_callback,],
                        resume_from_checkpoint="/home2/oralCancerGroup/OASIS/mask_metadata_dinura/benign_opmd/Final_20240415_BCE/Final_20240415_BCE/gihan_arch_with_metadata_epoch_200_bce_loss_sigmoid_xgb_model/tf_mobilenetv3_large_075_tf_mobilenetv3_large_075_0.001_200_2024-04-15_23-02-39/epoch=199-last.ckpt".format(checkpoint=loadFromCheckPoint),
                        default_root_dir='checkpoints/' + experiment_name,)
    else:
        trainer = pl.Trainer(max_epochs=args.epochs,
                        accelerator="gpu", devices=[0],
                        logger=[tensorboard_logger,],#, wandb_logger],
                        callbacks=[checkpoint_callback,],
                            )

    trainer.fit(lightning_module, train_loader, val_loader)