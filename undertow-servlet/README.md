Undertow Servlet Example

This is a simple standalone application based on the Undertow Examples.  It is used to test the OpenShift Java S2I image.

```bash
oc set env deployment/undertow-servlet SCRIPT_DEBUG=true PS4='+ $(basename $0):${LINENO} :'

oc get pods
#oc get pods -l app=undertow-servlet -ojson | jq -r '.items[].metadata.name'

#export POD=undertow-servlet-77977447b4-4tjsg
export POD=`oc get pods -l app=undertow-servlet -ojson | jq -r '.items[].metadata.name'` && echo $POD


```

```bash
#### Heap dump from the container

export HEAP_DUMP_PATH=/home/jboss/1

rm -rf heap.hprof

oc exec $POD -- rm -rf $HEAP_DUMP_PATH/heap.hprof
oc exec $POD -- mkdir -p $HEAP_DUMP_PATH
oc exec $POD -- ls -lrth
oc exec $POD -- /usr/lib/jvm/java-17-openjdk/bin/jmap -dump:format=b,file="$HEAP_DUMP_PATH/heap.hprof" $(oc exec $POD -- jps |grep -iv jps | awk '{print $1}'); oc rsync $POD:$HEAP_DUMP_PATH/heap.hprof .
ls -lrth


```

```bash
#### Thread dump from the container
export THREAD_DUMP_PATH=/home/jboss/1

echo $POD

rm -rf jstack.out

oc exec $POD -- rm -rf $THREAD_DUMP_PATH/heap.hprof
oc exec $POD -- mkdir -p $THREAD_DUMP_PATH
oc exec $POD -- ls -lrth

PID=$(oc exec $POD -- jps | grep -iv jps  | awk '{print $1}'); echo $PID
oc exec $POD -- bash -c "for x in {1..10}; do jstack -l $PID >> $THREAD_DUMP_PATH/jstack.out; sleep 2; done"; 
oc rsync $POD:$THREAD_DUMP_PATH/jstack.out .
ls -lrth 

```

```bash
##### identify high CPU utilization by Java threads
export HIGH_CPU_DUMP_PATH=/home/jboss/1

echo $POD

rm -rf high-cpu.out high-cpu-tdump.out

oc exec $POD -- rm -rf $HIGH_CPU_DUMP_PATH/heap.hprof
oc exec $POD -- mkdir -p $HIGH_CPU_DUMP_PATH
oc exec $POD -- ls -lrth

PID=$(oc exec $POD  jps | grep -iv jps  | awk '{print $1}');  echo $PID
## config sleep back to 20
oc exec $POD -- bash -c "for x in {1..6}; do jstack -l $PID >> $HIGH_CPU_DUMP_PATH/high-cpu-tdump.out; top -b -n 1 -H -p $PID >> $HIGH_CPU_DUMP_PATH/high-cpu.out; sleep 2;  done"; 
oc rsync $POD:$HIGH_CPU_DUMP_PATH/high-cpu.out .
oc rsync $POD:$HIGH_CPU_DUMP_PATH/high-cpu-tdump.out .
ls -lrth


## The command presented here works only if package props-ng is part of the OCP/RH-SSO image
## SOLUTION: The Ephemeral Containers is introduced in OCP 4.10 based on Kubernetes 1.23+ as a beta future. You can attach debug containers without restarting the target pod, and debug container runs in the target pod's namespace. The following example attaches the toolbox container including ps and top commands.

POD=$(oc get pods -l app=undertow-servlet -o jsonpath="{.items[*].metadata.name}")
CONTAINER_NAME=$(oc get pods -l app=undertow-servlet -o jsonpath="{.items[*].spec.containers[*].name}")
echo ; echo $POD; echo $CONTAINER_NAME; echo

#The oc command does not support launching Ephemeral Container, so this example uses kubectl instead of oc
kubectl debug -it $POD --image=registry.redhat.io/rhel8/support-tools:latest --target=$CONTAINER_NAME

for i in {1..10}; do top -b -n 1 -H -p 644 >> /tmp/top_644.out; sleep 2; done

oc cp $POD:tmp/top_644.out top_644.out -c debugger-q6twx

```
