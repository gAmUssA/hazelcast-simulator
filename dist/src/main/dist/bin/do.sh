#!/bin/bash

if [ -z "${STABILIZER_HOME}" ] ; then
    export STABILIZER_HOME=$(cd $(dirname $(readlink -f $0 2> /dev/null || readlink $0 2> /dev/null || echo $0))/.. && pwd)
fi

echo  STABILIZER_HOME = ${STABILIZER_HOME}

INSTANCE_COUNT=4

#Amazon Linux AMI 2014.03
AMI=ami-2f726546
#Red Hat Enterprise Linux 6.4 (64-bit)
#AMI=ami-a25415cb
#Red Hat Enterprise Linux 6.5 (64-bit)
#AMI=ami-63b6910a
#Amazon Linux AMI 2014.03 (64-bit)
#AMI=ami-2f726546
#Ubuntu Server 12.04 LTS (64-bit)
#AMI=ami-59a4a230
#Ubuntu Server 13.10  (64-bit)
#AMI=ami-35dbde5c

AVAILABILITY_ZONE=us-east-1a
SECURITY_GROUP=open
#15 gigs of ram, 8 vcpus  26 ecus.
#INSTANCE_TYPE=c3.2xlarge
#temporary usage of less powerful machine due to ec2 limitations
INSTANCE_TYPE=c1.xlarge
KEY_PAIR=testmachine2
USER=ec2-user
LICENSE=~/testmachine2.pem
INSTALL_DIR=
COACH_PORT=8701

function private_ip {
    local INSTANCE=$1
    local PRIVATE_IP=$( ec2-describe-instances | grep $INSTANCE | cut -f 18 )
    echo $PRIVATE_IP
}

function wait_started {
    local INSTANCE=$1

    echo "Waiting for instance $INSTANCE"

    for ATTEMPT in {1..300}
    do
        local STATUS_CHECKS=$( ec2-describe-instance-status $INSTANCE | sed -n 1p | cut -f 4 )
        local INSTANCE_STATUS=$( ec2-describe-instance-status $INSTANCE | sed -n 3p | cut -f 3 )

        if [ "${INSTANCE_STATUS}" = "passed" ] ;
        then
            echo Instance $INSTANCE is running
            return
        else
            echo [$ATTEMPT] Status: [$INSTANCE_STATUS]
        fi
    done

    echo ${INSTANCE} Failed to start
    ec2-describe-instance-status $INSTANCE
    exit
}

function install_stabilizer {
    local PRIVATE_IP=$1

    echo ==============================================================
    echo Installing Coach on $PRIVATE_IP
    echo ==============================================================

    local MEMBERS=""
    for IP in "${PRIVATE_IP_LIST[@]}"
    do
        MEMBERS="$MEMBERS<member>$IP:$COACH_PORT</member>\n"
    done

    cp coach-hazelcast-template.xml coach-hazelcast.xml
    cat coach-hazelcast.xml | sed -e "s@MEMBERS@$MEMBERS@" > coach-hazelcast.xml.bak && mv coach-hazelcast.xml.bak coach-hazelcast.xml

    echo "Making SSH Connection"
    ssh -i ${LICENSE} -o StrictHostKeyChecking=no ${USER}@${PRIVATE_IP} "ls"

    echo "Copying stabilizer files"
    #we need to pull out this property
    scp -i ${LICENSE} -r ${STABILIZER_HOME} ${USER}@${PRIVATE_IP}:
    #we need to override the hazelcast config file with the one we generated.
    scp -i ${LICENSE}  coach-hazelcast.xml ${USER}@${PRIVATE_IP}:hazelcast-stabilizer-0.1-SNAPSHOT/conf/

    echo ==============================================================
    echo Successfully installed Coach on $PRIVATE_IP
    echo ==============================================================
}

function init_manager_file {
    local PRIVATE_IP=$1

    local MEMBERS=""
    for PRIVATE_IP in "${PRIVATE_IP_LIST[@]}"
    do
        MEMBERS="$MEMBERS<address>$PRIVATE_IP:$COACH_PORT</address>\n"
    done

    cp manager-hazelcast-template.xml manager-hazelcast.xml
    cat manager-hazelcast.xml | sed -e "s@MEMBERS@$MEMBERS@" > manager-hazelcast.xml.bak && mv manager-hazelcast.xml.bak manager-hazelcast.xml
}

function start_coach {
    local PRIVATE_IP=$1

    echo ==============================================================
    echo Starting Coach on $PRIVATE_IP
    echo ==============================================================

    #we need to pull out this property
    ssh -o StrictHostKeyChecking=no -i ${LICENSE} ${USER}@${PRIVATE_IP} \
        "killall -9 java ; nohup hazelcast-stabilizer-0.1-SNAPSHOT/bin/coach  > foo.out 2> foo.err < /dev/null &"

    echo ==============================================================
    echo Successfully Started Coach on $PRIVATE_IP
    echo ==============================================================
}

echo Starting $INSTANCE_COUNT $INSTANCE_TYPE machines

START_STATUS=$( ec2-run-instances \
    --availability-zone $AVAILABILITY_ZONE \
    --instance-type $INSTANCE_TYPE \
    --instance-count $INSTANCE_COUNT \
    --group $SECURITY_GROUP \
    --key $KEY_PAIR \
    $AMI )

echo ==============================================================
echo "$START_STATUS"
echo ==============================================================

#temp hack writing to file to deal with loosing the linefeeds.
echo "$START_STATUS" > "out.txt"
STR=$( cat out.txt | grep INSTANCE |  awk '{print $2}' )
echo $STR

INSTANCES=(`echo $STR | tr "," "\n"`)

echo instances
for INSTANCE in "${INSTANCES[@]}"
do
    echo "> [$INSTANCE]"
    wait_started $INSTANCE
done

PRIVATE_IP_LIST=()
for INSTANCE in "${INSTANCES[@]}"
do
    PRIVATE_IP=$( private_ip $INSTANCE )
    PRIVATE_IP_LIST+=("$PRIVATE_IP")
done

for PRIVATE_IP in "${PRIVATE_IP_LIST[@]}"
do
    install_stabilizer $PRIVATE_IP
done

init_manager_file

for PRIVATE_IP in "${PRIVATE_IP_LIST[@]}"
do
    start_coach $PRIVATE_IP
done

echo Coaches started
for PRIVATE_IP in "${PRIVATE_IP_LIST[@]}"
do
    echo -- Coach $PRIVATE_IP
done