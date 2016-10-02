package org.primftpd.services;

import org.apache.sshd.server.PublickeyAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.primftpd.pojo.KeyParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.PublicKey;
import java.util.List;

public class PubKeyAuthenticator implements PublickeyAuthenticator {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final List<PublicKey> pubKeys;

    public PubKeyAuthenticator(List<PublicKey> pubKeys) {
        this.pubKeys = pubKeys;
    }

    @Override
    public boolean authenticate(String username, PublicKey key, ServerSession session) {
        logger.debug("attempting pub key auth, user: {}, client key class: '{}'",
                username, key.getClass().getName());
        // never mind username
        for (PublicKey configuredKey : pubKeys) {
            boolean keyEquals = keysEqual(configuredKey, key);
            logger.debug("pub key auth, success: {}, server key class '{}'",
                    keyEquals, configuredKey.getClass().getName());
            if (keyEquals) {
                return true;
            }
        }
        return false;
    }
    private boolean keysEqual(PublicKey serverKey, PublicKey clientKey) {
        if (serverKey != null && clientKey != null) {
            String clientKeyClass = clientKey.getClass().getName();
            if (serverKey instanceof org.bouncycastle.jce.provider.JCEECPublicKey
                    && "com.android.org.bouncycastle.jce.provider.JCEECPublicKey".equals(clientKeyClass))
            {
                try {
                    BigInteger[] clientKeyPoint = clientKeyPointQ(clientKey);
                    PublicKey clientKey2 = KeyParser.createPubKeyEcdsa(clientKeyPoint[0], clientKeyPoint[1]);
                    return serverKey.equals(clientKey2);
                } catch (Exception e) {
                    logger.error("could not get component of client key to compare with server key", e);
                }
            } else {
                return serverKey.equals(clientKey);
            }
        }
        return false;
    }
    private BigInteger[] clientKeyPointQ(PublicKey clientKey)
            throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException
    {
        Field field = clientKey.getClass().getDeclaredField("q");
        field.setAccessible(true);
        Object pointQ = field.get(clientKey);

        BigInteger x = pointCoord("X", pointQ);
        BigInteger y = pointCoord("Y", pointQ);
        return new BigInteger[]{x, y};
    }

    private BigInteger pointCoord(String coord, Object point)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        Method getCoord = null;
        String methodName = "get" + coord;
        Method[] methods = point.getClass().getMethods();
        for(Method method : methods) {
//            Class[] paras = method.getParameterTypes();
//            StringBuilder sb = new StringBuilder();
//            sb.append("method: ");
//            sb.append(method.getName());
//            sb.append("(");
//            String delimiter = "";
//            for (Class para : paras) {
//                sb.append(delimiter);
//                sb.append(para.getName());
//                delimiter = ", ";
//            }
//            sb.append(")");
//            logger.info("{}", sb.toString());

            if (methodName.equals(method.getName())) {
                getCoord = method;
                break;
            }
        }

        if (getCoord == null) {
            throw new NoSuchMethodException(methodName);
        }

        //Method getCoord = point.getClass().getDeclaredMethod("get" + coord);
        Object fieldElementX = getCoord.invoke(point);
        Method toBigInt = fieldElementX.getClass().getDeclaredMethod("toBigInteger");
        return (BigInteger)toBigInt.invoke(fieldElementX);
    }
}
