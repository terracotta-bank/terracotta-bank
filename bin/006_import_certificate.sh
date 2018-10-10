###
# Import SSL certificate into keystore
#
# Once imported, we can point our web 
# application at its alias, and it will
# present the certificate to the browser
# as proof of the site's authority.
#
###

keytool -import -file terracotta.crt \
        -alias terracotta \
        -keystore terracotta.jks
