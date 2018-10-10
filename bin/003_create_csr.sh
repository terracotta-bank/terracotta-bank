###
#
# Generate a Certificate Signing Requesti
# (CSR).
#
# Once we generate this, then we can 
# give the CSR to a CA which will
# use it to sign an SSL certficate.
#
###

keytool -certreq -file terracotta.csr \
        -alias terracotta \
        -keystore terracotta.jks
