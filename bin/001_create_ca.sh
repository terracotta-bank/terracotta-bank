###
#
# Create a 2048-bit root key for our CA.
#
# One created, this key can be used to 
# sign SSL certificates via a
# Certificate Signing Request (CSR)
#
###

openssl genrsa -des3 -out ca.key 2048

###
# Create a root certificate for our CA.
#
# Once created, this certificate can be
# configured in a browser so that it
# will trust SSL certificates signed by 
# the root key.
# 
###

openssl req -x509 -new -nodes -key ca.key \
            -sha256 -days 1825 -out ca.pem

echo "########################################"
echo "#   Welcome to the CA Grand Opening!   #"
echo "########################################"
