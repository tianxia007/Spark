package org.jivesoftware.sparkimpl.certificates;

import java.awt.HeadlessException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import javax.naming.InvalidNameException;
import javax.net.ssl.X509TrustManager;

import org.jivesoftware.spark.util.log.Log;
import org.jivesoftware.sparkimpl.settings.local.LocalPreferences;
import org.jivesoftware.sparkimpl.settings.local.SettingsManager;

/**
 * This class contain some of the methods and variables that are common for Spark's TrustManagers
 * @author Pawel Scibiorski
 *
 */
public abstract class GeneralTrustManager implements X509TrustManager {
    protected LocalPreferences localPref = SettingsManager.getLocalPreferences();
    protected CertificateController certControll = new CertificateController(localPref);
    protected KeyStore allStore;
    protected abstract void loadKeyStores();
    /**
     * Adds content of the keystore to allStore which should contain all certificates from accepted issuers.
     * @param store 
     * @throws KeyStoreException
     * @throws HeadlessException
     * @throws InvalidNameException
     */
    protected void addKeyStoreContentToAllStore(KeyStore keyStore)
            throws KeyStoreException, HeadlessException, InvalidNameException {

        Enumeration<String> aliases = keyStore.aliases();
        // Retrieve all of the certificates out of the KeyStore using aliases
        while (aliases.hasMoreElements()) {
            String alias = (String) aliases.nextElement();
            X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);
            allStore.setCertificateEntry(certControll.useCommonNameAsAlias(certificate), certificate);
        }

    }

    protected void loadAllStore() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {

        allStore = KeyStore.getInstance("JKS");
        allStore.load(null, CertificateController.passwd);

    }
    
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        X509Certificate[] X509Certs = null;
        try {
            // See how many certificates are in the keystore.
            int numberOfEntry = allStore.size();

            // If there are any certificates in the keystore.
            if (numberOfEntry > 0) {
                
                // Create an array of X509Certificates
                X509Certs = new X509Certificate[numberOfEntry];
                
                // Get all of the certificate alias out of the keystore.
                Enumeration<String> aliases = allStore.aliases();
                // Retrieve all of the certificates out of the keystore
                // via the alias name.
                int i = 0;
                while (aliases.hasMoreElements()) {
                    X509Certs[i] = (X509Certificate) allStore.getCertificate((String) aliases.nextElement());
                    i++;
                }
            }
        } catch (KeyStoreException e) {
            Log.error("Cannot create accepted issuers list", e);
            X509Certs = null;
        }
        return X509Certs;
    }
}
