package core;

import lombok.Getter;
import lombok.Setter;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static core.Main.UTXOs;

/**
 * @Memo: 지갑 클래스
 */
@Getter @Setter
class Wallet {
    private PublicKey publicKey;
    private PrivateKey privateKey;
    public Map<String, TransactionOutput> UTXO_Wallet = new HashMap<>(); // 잔고 저장소의 의미

    Wallet() {
        generateKeyPair();
    }

    /**
     * @Memo: 지갑의 잔고를 구한다.
     */
    float getBalance() {
        float total = 0;
        for (Map.Entry<String, TransactionOutput> item : UTXOs.entrySet()) {
            // 조각들을 이용하여 잔고 금액을 합산한다.
            TransactionOutput UTXO = item.getValue();

            if (UTXO.isMine(publicKey)) {
                UTXO_Wallet.put(UTXO.getId(), UTXO);
                total += UTXO.getValue();
            }
        }
        return total;
    }

    /**
     * @Memo: 지갑의 돈을 보낸다.
     */
    Transaction sendFunds(PublicKey recipient, float value) {
        if (getBalance() < value) {
            System.out.println("[INFO] 금액 부족]");
            return null;
        }

        List<TransactionInput> inputs = new ArrayList<>();

        // 여러 조각에 금액이 분산되어 있으므로 긁어 모은다.
        float total = 0;
        for (Map.Entry<String, TransactionOutput> item : UTXO_Wallet.entrySet()) {
            TransactionOutput UTXO = item.getValue();
            total += UTXO.getValue();
            inputs.add(new TransactionInput(UTXO.getId()));
            if (total > value) break;
        }

        Transaction newTransaction = new Transaction(publicKey, recipient, value, inputs);
        newTransaction.generateSignature(privateKey); // 추가될 트랜잭션을 개인키로 서명

        for (TransactionInput transactionInput : inputs) {
            UTXO_Wallet.remove(transactionInput.getTransactionOutputId());
        }

        return newTransaction;
    }

    /**
     * @Memo: 키페어 생성
     */
    private void generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC"); // 타원곡선 알고리즘
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");

            keyGen.initialize(ecSpec, random);
            KeyPair keyPair = keyGen.generateKeyPair();

            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}