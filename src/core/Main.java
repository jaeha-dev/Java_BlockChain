package core;

import util.BlockUtil;
import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// p * q = X 일 때, X를 찾는 것
// p -> nonce ++
// q -> 난이도; 앞에 0의 개수가 제약 조건
// 암호화, 서명 차이
// 공개키/개인키는 동작이 동일하다. 키만 바꾸면 암/복호화가 달라짐
//

public class Main {
    static List<Block> blockChain = new ArrayList<>(); // 블록체인
    static Map<String, TransactionOutput> UTXOs = new HashMap<>();
    static int difficulty = 1; // 채굴 난이도 (3으로 지정할 경우, 해시 값이 3자리)

    // 트랜잭션은 최소 0.1F
    static float minimumTransaction = 0.1F;

    private static Wallet walletA;
    private static Wallet walletB;
    private static Transaction genesisTransaction;

    public static void main(String[] args) {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        // 지갑 생성
        walletA = new Wallet();
        walletB = new Wallet();
        Wallet coinBase = new Wallet(); // 광산 역할

        // 최초의 트랜잭션은 광산에서 나온다.
        genesisTransaction = new Transaction(coinBase.getPublicKey(), walletA.getPublicKey(), 100F, null);
        genesisTransaction.generateSignature(coinBase.getPrivateKey()); // 트랜잭션에 서명하여 진본 (지갑 A는 광산의 공개키로 진본 확인)
        genesisTransaction.setTransactionId("0");
        genesisTransaction.getOutputs().add(new TransactionOutput(genesisTransaction.getRecipient(), genesisTransaction.getValue(), genesisTransaction.getTransactionId()));

        // 3. main save UTXO (UTXO: 내보낼 수 있도록 설정된 잔고)
        UTXOs.put(genesisTransaction.getOutputs().get(0).id, genesisTransaction.getOutputs().get(0));

        // 4. Create genesis (첫 번째 블록은 해시 값을 갖지 않으므로 이전 해시를 0 으로 지정한다.)
        Block genesisBlock = new Block("0", "0");
        genesisBlock.addTransaction(genesisTransaction);
        addBlock(genesisBlock);
        System.out.println("[INFO] 제네시스 블록 생성됨");

        // 5. Block1
        Block block1 = new Block("1", genesisBlock.getHash());
        System.out.println("[INFO] 지갑A 잔고: " + walletA.getBalance());
        System.out.println("[INFO] 지갑B 잔고: " + walletB.getBalance());

        block1.addTransaction(walletA.sendFunds(walletB.getPublicKey(), 40F));
        addBlock(block1); // 망에 던진 것 (= 공유 가능)
        System.out.println("[INFO] 지갑A 잔고: " + walletA.getBalance());
        System.out.println("[INFO] 지갑B 잔고: " + walletB.getBalance());

        System.out.println(BlockUtil.getJson(blockChain));
    }

    private static void addBlock(Block newBlock) {
        newBlock.mineBlock(difficulty);
        blockChain.add(newBlock);
    }
}