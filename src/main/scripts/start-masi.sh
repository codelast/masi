#!/bin/bash

bin=$(dirname $0)
export PROJECT_HOME=`cd "$bin/.."; pwd`

. $bin/env.sh

OUTPUT_AUDIO_FILE=$bin/audio.wav

TQ_API_KEY=XXX  # 在"天启开放平台"上申请到的API Key
TQ_API_SECRET=XXX  # 在"天启开放平台"上申请到的API Secret

TX_API_SECRET_ID=XXX  # 在腾讯云上申请到的secretId
TX_API_SECRET_KEY=XXX  # 在腾讯云上申请到的secretKey

java -cp $CLASSPATH -Dlog4j.configuration=file://$PROJECT_CONF_HOME/log4j.properties \
  com.codelast.masi.MaSi \
  --tqApiKey $TQ_API_KEY \
  --tqApiSecret $TQ_API_SECRET \
  --txApiSecretId $TX_API_SECRET_ID \
  --txApiSecretKey $TX_API_SECRET_KEY \
  --outputAudioFile $OUTPUT_AUDIO_FILE
