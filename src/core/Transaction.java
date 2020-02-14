package core;

import lombok.Getter;
import lombok.Setter;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import static util.BlockUtil.*;

/**
 * @Memo: 트랜잭션 클래스
 */
@Getter @Setter
public class Transaction {
    private String transactionId; // 트랜잭션 ID
    private PublicKey sender; // 발신자 공개키
    private PublicKey recipient; // 수신자 공개키
    private float value; // 금액
    private byte[] signature; // 서명

    /**
     * @Memo: 비트코인의 트랜잭션은 입력/출력 목록을 갖는다.
     * (즉, 어디에서 왔는지, 어디로 보내는지를 함께 기록한다.)
     * (또한, 입력/출력은 N개이므로 리스트를 사용한다.)
     */
    private List<TransactionInput> inputs;
    private List<TransactionOutput> outputs = new ArrayList<>();

    // 트랜잭션 중복 방지를 위한 시퀀스 변수
    private static int sequence = 0;

    Transaction(PublicKey from, PublicKey to, float value, List<TransactionInput> inputs) {
        this.sender = from;
        this.recipient = to;
        this.value = value;
        this.inputs = inputs; // 어떤 트랜잭션 출력으로 코인을 보낼 것인지 지정한다.
    }

    /**
     * @Memo: 해시 생성
     */
    private String calculateHash() {
        sequence++;
        return applySha256(getStringFromKey(sender) + getStringFromKey(recipient) + value + sequence);
    }

    /**
     * @Memo: UTXO 중에 나의 것 중 값이 있는 것의 합
     */
    private float getInputValue() {
        float total = 0;
        for (TransactionInput transactionInput : inputs) {
            if (transactionInput.UTXO == null) continue;
            total += transactionInput.UTXO.value;
        }
        return total;
    }

    /**
     * @Memo: ?
     */
    public float getOutputsValue() {
        float total = 0;
        for(TransactionOutput transactionOutput : outputs) {
            total += transactionOutput.value;
        }
        return total;
    }

    /**
     * @Memo: 트랜잭션 처리
     */
    boolean processTransaction() {
        // 현재 트랜잭션의 위변조를 검증한다.
        if (! verifySignature()) return false;

        // 트랜잭션 입력의 UTXO 를 구한다.
        for (TransactionInput transactionInput : inputs) {
            transactionInput.UTXO = Main.UTXOs.get(transactionInput.getTransactionOutputId());
        }

        // 트랜잭션 값의 유효성 확인 (너무 작은 단위는 성능에 영향을 주므로 최소 값 확인)
        if (getInputValue() < Main.minimumTransaction) {
            return false;
        }

        // outputs 를 생성한다.
        float leftOver = getInputValue() - value; // 잔고
        transactionId = calculateHash();
        outputs.add(new TransactionOutput(this.recipient, value, transactionId)); // 내 아웃풋에 보내는 정보 추가
        outputs.add(new TransactionOutput(this.sender, leftOver, transactionId)); // 내 아웃풋에 잔고 정보 추가

        // UTXOs 에 새로운 UTXO 를 추가한다. (내 아웃풋을 메인의 UTXO 에 반영)
        for (TransactionOutput o : outputs) {
            Main.UTXOs.put(o.id, o);
        }

        // inputs 로 사용한 UTXO 는 소비되므로 UTXOs 에서 삭제한다. (내 아웃풋을 메인의 UTXO 에 반영)
        for (TransactionInput i : inputs) {
            if (i.UTXO == null) continue;
            Main.UTXOs.remove(i.UTXO.id);
        }

        return true;
    }

    /**
     * @Memo: 개인키를 사용하여 트랜잭션을 서명한다.
     */
    void generateSignature(PrivateKey privateKey) {
        String data = getStringFromKey(sender) + getStringFromKey(recipient) + value;
        signature = applyECDSASig(privateKey, data);
    }

    /**
     * @Memo: 트랜잭션의 위변조 유무를 검증한다.
     */
    private boolean verifySignature() {
        String data = getStringFromKey(sender) + getStringFromKey(recipient) + value;
        return verifyECDSASig(sender, data, signature); // T/F
    }
}