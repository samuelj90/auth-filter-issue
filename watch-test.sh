#!/bin/bash

# Install inotify-tools if not already installed
if ! command -v inotifywait &> /dev/null
then
    sudo apt-get update
    sudo apt-get install -y inotify-tools
fi

# Watch for changes and run tests
while true; do
    mvn test
    inotifywait -r -e modify,create,delete,move src/
done