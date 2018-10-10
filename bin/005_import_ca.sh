###
#
# Import the CA root certificate into
# the keystore
#
# Once imported, then the keystore will
# trust the SSL certificate generated 
# by that CA.
#
###

keytool -import -file ca.pem \
        -alias ca \
        -keystore terracotta.jks
