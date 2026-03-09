#!/bin/bash
API_ROOT="http://localhost:9200/"

for DIR in */
do
  cd $DIR
  for FILE in *.json
  do
	FILE_NAME="$FILE"
    TD_ID=$(jq '.training_definition_id' $FILE_NAME)
    TI_ID=$(jq '.training_instance_id' $FILE_NAME)
	EVENT=$(echo $FILE_NAME | grep -o "^[a-zA-Z]*")
    UPPERCASE="cz.cyberrange.platform.events.trainings.${EVENT}_evt.definitionId=${TD_ID}.instance=${TI_ID}"
	LOWERCASE=${UPPERCASE,,}
	curl -X POST -d @$FILE_NAME "${API_ROOT}${LOWERCASE}/default" -H 'Content-Type: application/json'
  done

  cd ..
done
