package core;

import lombok.Getter;
import lombok.Setter;
import util.BlockUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import static util.BlockUtil.applySha256;
import static util.BlockUtil.getDifficultyString;

/**
 * @Memo: 블록 클래스
 */
@Getter @Setter
class Block {
    private String hash; // 현재 해시
    private String previousHash; // 이전 해시
    private String data; // 블록 데이터 (블록 번호, 아이디 등?)
    private int nonce; // 갱신 횟수 (해시 재생성 횟수) -> Proof/Work 의 증거
    private long timeStamp; // 시간 정보
    private String merkleRoot; // 머클 루트 (머클트리의 하위 노드를 검증할 수 있는 루트 노드) -> 해시

    // 트랜잭션 목록
    private List<Transaction> transactionList = new ArrayList<>();

    /**
     * @Memo: 새로운 블록을 생성한다.
     * (생성된 블록은 고유의 해시(-> 이전 해시로 이해)를 갖는다.)
     */
    Block(String data, String preHash) {
        this.data = data;
        this.previousHash = preHash;
        this.timeStamp = new Date().getTime();
        this.hash = calculateHash(); // 블록 생성 시, 해시 값을 생성하여 지정한다.
    }

    /**
     * @Memo: 새로운 해시 값을 생성한다.
     * (해시 구조는 이전 해시 값, 시간 정보, 해시 재생성 횟수, 데이터로 구성된다.)
     */
    private String calculateHash() {
        return applySha256(previousHash + timeStamp + nonce + merkleRoot);
    }

    /**
     * @Memo: 채굴
     * (해시 값을 비교하면서 특정 조건을 만족하는 해시가 아닐 경우, 해시 값을 갱신 및 nonce 값 증가)
     * (특정 조건을 만족하는 해시가 생성될 경우, 채굴한다.)
     */
    void mineBlock(int difficulty) {
        merkleRoot = BlockUtil.getMerkleRoot(transactionList);

        // difficulty 숫자만큼 앞에 0 을 채운다.
        String target = getDifficultyString(difficulty);

        /**
         * @Memo: 생성된 Hash 와 Target 을 비교한다.
         * (만약, difficulty 가 3일 경우, target 은 000 이 되고,
         * 생성된 Hash 는 000 으로 시작한다.)
         */
        while (! hash.substring(0, difficulty).equals(target)) {
            System.out.println("[INFO] 채굴 실패 (Target: " + target + " | Hash: " + hash + " | Nonce: " + nonce + ")");
            nonce++;
            hash = calculateHash();
        }
        System.out.println("[INFO] 채굴 성공 (Target: " + target + " | Hash: " + hash + " | Nonce: " + nonce + ")");
        getBlockInfo();
    }

    /**
     * @Memo: 트랜잭션을 추가한다.
     */
    boolean addTransaction(Transaction transaction) {
        if (transaction == null) return false;

        if ((!"0".equals(previousHash))) {
            if (! transaction.processTransaction()) {
                return false;
            }
        }
        transactionList.add(transaction);
        System.out.println("[INFO] 해당 블록에 트랜잭션 추가됨");
        return true;
    }

    /**
     * @Memo: 블록 정보 출력
     */
    private void getBlockInfo() {
        System.out.println("*---------------------------------------------------------------------------------------------------------------*");
        System.out.println("+ 블록 번호(Block number): " + data);
        System.out.println("+ 갱신 횟수(Nonce): " + nonce);
        System.out.println("+ 이전 해시(Previous hash): " + previousHash);
        System.out.println("+ 시간 정보(Time stamp): " + timeStamp);
        System.out.println("+ 트랜잭션 개수(# of Transaction(s)): " + transactionList.size());
        System.out.println("+ 해시 구조(Block hash): " + calculateHash());
        System.out.println("*---------------------------------------------------------------------------------------------------------------*");
    }
}