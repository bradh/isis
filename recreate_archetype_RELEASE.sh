#!/usr/bin/env bash
export ISISREL=1.16.3
export ISISDEV=1.16.4-SNAPSHOT
export JIRA=ISIS-1916

export ISISTMP=/c/tmp   # or as required
export ISISPAR=$ISISREL
export ISISRC=RC1

for a in simpleapp helloworld
do
	pushd example/application/$a
	export ISISART=$a-archetype
    export ISISCPT=$(echo $ISISART | cut -d- -f2)
    export ISISCPN=$(echo $ISISART | cut -d- -f1)

	env | grep ISIS | sort
	sh ../../../scripts/recreate-archetype.sh $JIRA
	popd
done





