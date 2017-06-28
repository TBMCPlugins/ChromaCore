#!/bin/sh
FILENAME=$(find target/ ! -name '*original*' -name '*.jar')
echo Found file: $FILENAME

if [ $1 = 'production' ]; then
echo Production mode
echo $UPLOAD_KEY > upload_key
scp -i upload_key $FILENAME travis@server.figytuna.com:/minecraft/main/plugins
fi
