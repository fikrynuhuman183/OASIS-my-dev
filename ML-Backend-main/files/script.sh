#!/bin/bash


# model_names=("resnet50" "hrnet_w18" "densenet121" "mixnet_s" "tf_mobilenetv3_large_075" "inception_v3" "tf_mobilenetv3_small_075")
model_names=( "tf_mobilenetv3_large_075" )

# model_name="hrnet_w18"
# model_names=("hrnet_w18" "densenet121" "mixnet_s" "tf_mobilenetv3_large_075" "tf_mobilenetv3_small_075")
# model_names=("resnet50")

batch_size=64
num_workers=6
modification="Att_test__unaugmented"
# metadata=('xgb_model' 'svc_model')
metadata=('xgb_model')

epochs=200

for model_name in "${model_names[@]}";
do
    for meta in "${metadata[@]}"; 
    do
        logdir="Att_test_imnorm_weight_loss_train_unaugmented"$meta
        mkdir $logdir

        # Set the threshold for GPU memory usage in megabytes
        threshold=5000
        logdir="$logdir"/"$model_name"
        mkdir $logdir
        bg_log=$logdir/train_background_$model_name"_"$modification"_2.log"
        while true; 
        do
            # Get GPU memory usage information using nvidia-smi
            gpu_info=$(nvidia-smi --query-gpu=memory.used --format=csv,noheader,nounits)
            
            # Extract the first GPU's memory usage
            current_memory=$(echo "$gpu_info" | awk 'NR==1{print $1}')
            
            echo "Current GPU Memory Usage: $current_memory MB" > $bg_log
            
            if [ "$current_memory" -lt "$threshold" ]; then
                echo "GPU memory usage is below the threshold. Running the experiment..." >> $bg_log
                
                break
            fi
            
            sleep 5  # Adjust the interval as needed
        done

        echo "Training with model: $model_name $meta..."
        echo "Starting background logger for experiment: $logdir" >> $bg_log

        
        full_text_log=$logdir/full_text_$model_name"_"$modification"_2.txt"
        nohup python ../train.py  --metadata="$meta" --model_name="$model_name" --num_workers="$num_workers" --logdir="$logdir" --epochs="$epochs" --pref="$logdir" --batch_size="$batch_size" > $full_text_log 2>&1 &train_pid=$!
        echo "nohupped training started and running in background (train_pid=$train_pid)">> $bg_log
        sleep 240

        logger_pids=()
        # echo "nohupped GPU logging started and running in background (gpu_logger_pid=$gpu_logger_pid)" >> $bg_log

        cleanup() {
            if ps --pid $train_pid &> /dev/null
            then
                kill -9 $train_pid
            fi
            for pid in "${logger_pids[@]}"
            do
                if ps --pid $pid &> /dev/null
                then
                    kill -9 $pid
                fi
            done

            exit 1
        }
        trap cleanup INT KILL

        echo "Polling for training process to be terminated/killed..." >> $bg_log

    # Poll for train_pid and kill the nvidia-smi logger when train_pid is terminated.
        while true
        do
            sleep 20

            if ! ps --pid $train_pid &> /dev/null
            then
                echo "Training process (pid=$train_pid) has been terminated/killed." >> $bg_log
                for pid in "${logger_pids[@]}"
                do
                    if ps --pid $pid &> /dev/null
                    then
                        kill -9 $pid
                        echo "Killed the logger process (pid=$pid)." >> $bg_log
                    fi
                done
                break
            fi
        done

        echo "Ending the background logger.">>$bg_log

done
        # logdir="Without_dropout" 

done
echo "Training completed for all models."
