package util;

import com.google.gson.GsonBuilder;
import core.Transaction;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * @Memo: 유틸리티 클래스
 */
public class BlockUtil {

    /**
     * @Memo: SHA256 알고리즘 & UTF8 해시
     */
    public static String applySha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @Memo: Block 클래스의 mineBlock() 메소드에서,
     * target 문자열을 difficulty 숫자만큼 앞에 0 을 채운다.
     */
    public static String getDifficultyString(int difficulty) {
        return new String(new char[difficulty]).replace('\0', '0');
    }

    /**
     * @Memo: Key -> String
     */
    public static String getStringFromKey(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /**
     * @Memo: 객체를 JSON 타입으로 변환 후, 변환된 JSON 데이터를 문자열로 반환한다.
     */
    public static String getJson(Object o) {
        return new GsonBuilder().setPrettyPrinting().create().toJson(o);
    }

    /**
     * @Memo: 트랜잭션 객체를 개인키로 서명한다.
     */
    public static byte[] applyECDSASig(PrivateKey privateKey, String input) {
        Signature dsa;
        byte[] output;

        try {
            dsa = Signature.getInstance("ECDSA", "BC");
            dsa.initSign(privateKey);
            byte[] strByte = input.getBytes();
            dsa.update(strByte);
            output = dsa.sign();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return output;
    }

    /**
     * @Memo: 공개키와 서명을 사용하여 데이터를 검증한다.
     */
    public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature) {
        try {
            Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(data.getBytes());
            return ecdsaVerify.verify(signature);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @Memo: 머클 트리(해시 트리)
     * @Memo: 머클 트리는 말단 노드를 제외한 노드들의 이름이 자식 노드들의 이름의 해시로 구성된 트리이다.
     * (즉, 머클 트리의 루트 노드는 하위 모든 자식들의 이름을 해시에 포함한다고 볼 수 있다.)
     */
    public static String getMerkleRoot(List<Transaction> transactionList) {
        int count = transactionList.size();

        List<String> previousTreeLayer = new ArrayList<>();
        for (Transaction transaction : transactionList) {
            previousTreeLayer.add(transaction.getTransactionId());
        }
        List<String> treeLayer = previousTreeLayer;

        while (count > 1) {
            treeLayer = new ArrayList<>();
            for (int i = 1; i < previousTreeLayer.size(); i += 2) {
                // 하위 두 개의 자식 노드의 이름을 붙여서 다시 해싱한다.
                treeLayer.add(applySha256(previousTreeLayer.get(i - 1) + previousTreeLayer.get(i)));
            }
            count = treeLayer.size();
            previousTreeLayer = treeLayer;
        }

        return (treeLayer.size() == 1) ? treeLayer.get(0) : "";
    }
}