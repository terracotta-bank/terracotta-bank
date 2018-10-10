###
#
# Generate a key and keystore 
# for Terracotta Bank
#
###

keytool -genkey -keyalg RSA -keysize 2048 \
        -alias terracotta \
        -keystore terracotta.jks
