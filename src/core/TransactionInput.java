package core;

import lombok.Getter;
import lombok.Setter;

/**
 * @Memo: 트랜잭션 입력 클래스
 * (UTXO: 트랜잭션의 구조에서 아직 출력이 없는 것을 UTXO라 한다. 즉, 아직 소비되지 않은 것으로 이해한다.)
 * (입력/출력은 근본적으로 구조는 동일하다고 볼 수 있다.)
 * (UTXO -> 입력/출력)
 */
@Getter @Setter
class TransactionInput {
    private String transactionOutputId;
    public TransactionOutput UTXO;

    TransactionInput(String transactionOutputId) {
        this.transactionOutputId = transactionOutputId;
    }
}