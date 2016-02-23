kill `cat pids/pid_debug`

while [[ ${?} == 0 ]]      # Repeat until the process has terminated.
do
    sleep 1s              # Wait a bit before testing.
    ps -p `cat pids/pid_debug` >/dev/null  # Check if the process has terminated.
done

export DEBUG=socket.io*,PacketService,NotificationService,RestApi,Stats,TTLService
nohup node . > logs/debug.log 2>&1&
echo $! > pids/pid_debug
