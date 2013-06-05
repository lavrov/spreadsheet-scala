java  -XX:MaxPermSize=512M -Xmx1G -Xss4M -XX:+CMSClassUnloadingEnabled -jar `dirname $0`/sbt-launch.jar "$@"
