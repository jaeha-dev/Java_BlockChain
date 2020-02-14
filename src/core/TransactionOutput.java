package core;

import lombok.Getter;
import lombok.Setter;
import java.security.PublicKey;
import static util.BlockUtil.applySha256;
import static util.BlockUtil.getStringFromKey;

/**
 * @Memo: 트랜잭션 출력 클래스
 */
@Getter @Setter
class TransactionOutput {
    public String id; // 트랜잭션 출력 ID
    public PublicKey recipient; // 수신자 공개키
    public float value; // 금액
    public String parentTransactionId; // 해당 출력을 발생시킨 트랜잭션의 ID (이전 트랜잭션 ID)

    TransactionOutput(PublicKey recipient, float value, String parentTransactionId) {
        this.id = applySha256(getStringFromKey(recipient) + value + parentTransactionId);
        this.recipient = recipient;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
    }

    /**
     * @Memo: 해당 트랜잭션 출력이 자신의 것인지 확인한다.
     */
    boolean isMine(PublicKey publicKey) {
        return (publicKey == recipient); // T/F
    }
}