kill `cat pid_debug`

while [[ ${?} == 0 ]]      # Repeat until the process has terminated.
do
    sleep 10s              # Wait a bit before testing.
    ps -p `cat pid_debug` >/dev/null  # Check if the process has terminated.
done

export DEBUG=ProxyServer,RedisStore,RestApi
nohup node . > debug.log 2>&1&
echo $! > pid_debug
