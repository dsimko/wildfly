#!/bin/sh

cd $JBOSS_HOME/standalone/configuration/

keytool -genkeypair -noprompt -alias localhost -keyalg RSA -keysize 1024 -validity 365 -keystore server.keystore.jks -dname "CN=localhost" -keypass secret -storepass secret
keytool -genkeypair -noprompt -alias client -keyalg RSA -keysize 1024 -validity 365 -keystore client.keystore.jks -dname "CN=client" -keypass secret -storepass secret

keytool -exportcert -noprompt -keystore server.keystore.jks -alias localhost -keypass secret -storepass secret -file server.cer
keytool -exportcert -noprompt -keystore client.keystore.jks -alias client -keypass secret -storepass secret -file client.cer

keytool -importcert -noprompt -keystore server.truststore.jks -storepass secret -alias client -trustcacerts -file client.cer
keytool -importcert -noprompt -keystore client.truststore.jks -storepass secret -alias localhost -trustcacerts -file server.cer

