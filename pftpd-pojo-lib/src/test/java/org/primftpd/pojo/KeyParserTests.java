package org.primftpd.pojo;

import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.math.BigInteger;
import java.security.PublicKey;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;

public class KeyParserTests {

    @Test
    public void parsePubKeyRsa() throws Exception {
        InputStream is = getClass().getResourceAsStream("/keys/rsa.key.pub");

        List<PublicKey> keys = KeyParser.parsePublicKeys(is, new CommonsBase64Decoder());

        assertsRsaKey((RSAPublicKey)keys.get(0));
    }

    @Test
    public void parsePubKeyDsa() throws Exception {
        InputStream is = getClass().getResourceAsStream("/keys/dsa.key.pub");

        List<PublicKey> keys = KeyParser.parsePublicKeys(is, new CommonsBase64Decoder());

        assertsDsaKey((DSAPublicKey)keys.get(0));
    }

    @Test
    public void parsePubKeyEcdsa() throws Exception {
        InputStream is = getClass().getResourceAsStream("/keys/ecdsa.key.pub");

        List<PublicKey> keys = KeyParser.parsePublicKeys(is, new CommonsBase64Decoder());

        assertsEcdsaKey((ECPublicKey)keys.get(0));
    }

    @Test
    public void parseAuthorizedKeys() throws Exception {
        InputStream is = getClass().getResourceAsStream("/keys/authorized_keys");

        List<PublicKey> keys = KeyParser.parsePublicKeys(is, new CommonsBase64Decoder());

        assertsRsaKey((RSAPublicKey)keys.get(0));
        assertsDsaKey((DSAPublicKey)keys.get(1));
        assertsEcdsaKey((ECPublicKey)keys.get(2));
    }

    protected void assertsRsaKey(RSAPublicKey pubKey) {
        Assert.assertEquals(new BigInteger("65537"), pubKey.getPublicExponent());

        final String modulus = "22840028388110743583131987675136887114153126223124011317437832666" +
                "25854781992306722377061897219550740787245366580892823047038154958086308817575397" +
                "86207323511183698710582016112357060923053840856777454937186677760616034425665662" +
                "82261472797239839649294119764258671908502475664743909024714305171394796349355615" +
                "01410512875834037603865586850446929492793894140130256172372280205701912961974382" +
                "44718040286649900869581969011709834002741504113088991590355018061303753262915348" +
                "56911333402703872012358714368938812147820774134682975669390306870781321673316754" +
                "378035200080485404740444851779733064858474545694849794752210968120764651";
        Assert.assertEquals(new BigInteger(modulus), pubKey.getModulus());
    }

    protected void assertsDsaKey(DSAPublicKey pubKey) {
        final String y = "4075820517720311789755060432555041302495713535036194101055101600952719" +
                "8027506134078097330328538489864134942817893994891118803853518548361792777130885" +
                "0845452847199857520010376744070518762657897263318144714919719488458432611731877" +
                "3733795914935443964469170020723158291398484608457816805394280489144894060446820" +
                "2";
        final String p = "1562763388678684549676999956870179987376000994454452811488079320239653" +
                "8931971498715717466033996067243932790630000740882000826960832106054102246126902" +
                "3050793071435716238554837246001821695252267029019836147068133782812531548770882" +
                "6153064920839234179080884223223263305562612862165508525479239452754625899807548" +
                "07";
        final String q = "1325486242274701569333126235614816814166592776627";
        final String g = "1053939190524437845710740492266780383434946918308472822218659846206636" +
                "3468617688992758226678340652253001602083786669177050081112107298337364078312067" +
                "8593218571928390833559198136388601343715984061418418925932387956796945760464070" +
                "8605211665506462942166129968135830426818793738520715937903855564717876010412364" +
                "82";

        Assert.assertEquals(new BigInteger(y), pubKey.getY());
        Assert.assertEquals(new BigInteger(p), pubKey.getParams().getP());
        Assert.assertEquals(new BigInteger(q), pubKey.getParams().getQ());
        Assert.assertEquals(new BigInteger(g), pubKey.getParams().getG());
    }

    protected void assertsEcdsaKey(ECPublicKey pubKey) {
        final String x = "48439561293906451759052585252797914202762949526041747995844080717082404635286";
        final String y = "36134250956749795798585127919587881956611106672985015071877198253568414405109";

        Assert.assertEquals(new BigInteger(x), pubKey.getParams().getGenerator().getAffineX());
        Assert.assertEquals(new BigInteger(y), pubKey.getParams().getGenerator().getAffineY());
    }

    public static class CommonsBase64Decoder implements Base64Decoder {
        @Override
        public byte[] decode(String str) {
            return Base64.decodeBase64(str);
        }
    }
}
