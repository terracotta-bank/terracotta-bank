###
#
# Sign the SSL Certificate
# 
# Once created, we can import this 
# certificate into our keystore.
#
###

openssl x509 -req -in terracotta.csr \
             -CA ca.pem -CAkey ca.key -CAcreateserial \
             -out terracotta.crt -days 1825 \
             -sha256 -extfile terracotta.ext
