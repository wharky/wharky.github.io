---
layout: post
title: "AI 두뇌의 리얼타임 텔레파시: Behavior Trees와 네트워크 시점 동기화의 은밀한 공조"
categories: tech
---

![AI 두뇌의 리얼타임 텔레파시: Behavior Trees와 네트워크 시점 동기화의 은밀한 공조](https://image.pollinations.ai/prompt/Abstract+representation+of+a+glowing+neural+network+within+a+digital+brain%2C+connected+by+ethereal+data+streams+to+server+racks+in+a+futuristic%2C+cinematic+lighting+setup.+High-tech%2C+conceptual%2C+deep+blue+and+purple+hues%2C+intricate+connections%2C+subtle+motion+blur%2C+focus+on+synchronization+and+intelligent+processing.+Unreal+Engine+5+render%2C+octane+render%2C+volumetric+light.?width=800&height=450&nologo=true&seed=9215)

> **AI Image Prompt:** Abstract representation of a glowing neural network within a digital brain, connected by ethereal data streams to server racks in a futuristic, cinematic lighting setup. High-tech, conceptual, deep blue and purple hues, intricate connections, subtle motion blur, focus on synchronization and intelligent processing. Unreal Engine 5 render, octane render, volumetric light.

안녕하세요, 동료 개발자 여러분! 오늘 커피 한 잔 들고 나누고 싶은 이야기는, 우리 게임 속 AI가 마치 눈앞에서 실시간으로 반응하는 듯한 착각을 불러일으키면서도, 네트워크의 변덕스러운 환경 속에서 일관성과 권위를 잃지 않게 하는 마법 같은 기술의 결합입니다. 바로 **'AI Behavior Trees'**와 **'Server-Side Network Sync (Rollback/Dead Reckoning)'**의 시너지에 대한 심층 분석이죠.

현대 게임에서 AI는 더 이상 그저 정해진 패턴만 반복하는 바보가 아닙니다. 복잡한 환경에 반응하고, 플레이어의 행동에 따라 전략을 바꾸며, 때로는 예측 불가능한 변수까지 고려하는 똑똑한 존재여야 하죠. 그런데 이런 똑똑한 AI가 멀티플레이어 환경에서 네트워크 지연이라는 숙적을 만나면 어떻게 될까요? 클라이언트에서는 뚝뚝 끊기거나, 서버에서는 이미 다른 행동을 하고 있는데 클라이언트에서는 딴청을 부리는 '버벅이는 AI'가 되어버리기 십상입니다. 오늘은 이 골치 아픈 문제를 어떻게 해결하고, 나아가 AI를 네트워크 게임의 핵심적인 플레이어로 끌어올릴 수 있는지 이야기해보겠습니다.

---

### **문제의 발단: 똑똑한 AI vs. 지연된 세상**

우리가 만들고자 하는 AI는 단순한 상태 머신(Finite State Machine)의 한계를 넘어, 복잡한 의사결정과 목표 기반 행동을 수행해야 합니다. 여기서 등장하는 것이 바로 **Behavior Trees (BTs)**죠. BT는 행동을 계층적이고 모듈화된 트리 구조로 표현하여, 복잡한 AI 로직을 직관적으로 설계하고 디버깅할 수 있게 해줍니다.

하지만 멀티플레이어 환경에서 이 똑똑한 AI가 빛을 발하기는 쉽지 않습니다.
1.  **레이턴시의 저주**: 서버에서 AI의 행동을 결정하고 클라이언트에 전달하는 동안 발생하는 지연 시간은 AI의 반응성을 떨어뜨립니다. 클라이언트에서는 AI가 늦게 반응하는 것처럼 보일 수 있죠.
2.  **데이터 불일치**: 각 클라이언트가 독립적으로 AI의 행동을 추측하거나, 서버의 데이터를 늦게 받아 처리한다면, 필연적으로 서버와 클라이언트 간에 AI의 상태가 불일치하는 '데싱크(Desync)' 현상이 발생합니다. 특히 AI가 다른 플레이어에게 영향을 미치는 경우, 이는 치명적인 버그나 불공정성으로 이어질 수 있습니다.
3.  **성능 부담**: 모든 AI의 모든 상태를 매 프레임 서버에서 클라이언트로 전송하는 것은 네트워크 대역폭과 서버 성능에 엄청난 부담을 줍니다.

저희 팀에서도 초기에 AI를 서버-클라이언트 간 동기화할 때 이 문제로 참 많이 삽질했던 기억이 납니다. AI가 벽에 부딪혔는데 클라이언트에서는 여전히 유유히 걷고 있다거나, 심지어 특정 상황에서 AI가 네트워크 랙 때문에 춤을 추는 기이한 버그까지 경험했었죠. 이 모든 혼돈의 근원은 "AI의 뇌가 서버에 있는데, 클라이언트의 눈은 현재와 미래를 보고 싶어 한다"는 간극 때문이었습니다.

---

### **해결책의 핵심: AI Behavior Trees, '뇌'가 되다**

AI Behavior Trees는 우리 AI의 '뇌' 역할을 합니다. 서버는 모든 AI의 BT를 실행하며, AI의 다음 행동(이동, 공격, 회피 등)을 결정하는 권위 있는 주체가 됩니다.

#### **Behavior Tree 핵심 로직 (Pseudo-code)**

간단한 BT 노드들을 생각해볼까요?

```pseudocode
// 모든 Behavior Tree 노드의 기본 인터페이스
interface BehaviorNode {
    execute(): NodeResult; // SUCCESS, FAILURE, RUNNING
}

enum NodeResult {
    SUCCESS,
    FAILURE,
    RUNNING
}

// 순차적으로 자식 노드를 실행, 모두 성공해야 성공
class SequenceNode implements BehaviorNode {
    nodes: BehaviorNode[];
    constructor(nodes: BehaviorNode[]) { /* ... */ }
    execute(): NodeResult {
        for (node of this.nodes) {
            result = node.execute();
            if (result == NodeResult.FAILURE) return NodeResult.FAILURE; // 하나라도 실패하면 바로 실패
            if (result == NodeResult.RUNNING) return NodeResult.RUNNING; // 실행 중이면 다음 틱에 계속
        }
        return NodeResult.SUCCESS; // 모든 노드가 성공
    }
}

// 자식 노드 중 하나라도 성공하면 성공
class SelectorNode implements BehaviorNode {
    nodes: BehaviorNode[];
    constructor(nodes: BehaviorNode[]) { /* ... */ }
    execute(): NodeResult {
        for (node of this.nodes) {
            result = node.execute();
            if (result == NodeResult.SUCCESS) return NodeResult.SUCCESS; // 하나라도 성공하면 바로 성공
            if (result == NodeResult.RUNNING) return NodeResult.RUNNING; // 실행 중이면 다음 틱에 계속
        }
        return NodeResult.FAILURE; // 모든 노드가 실패
    }
}

// 실제 게임 액션을 수행하는 리프 노드 (예: 이동, 공격)
class ActionMoveToPlayer implements BehaviorNode {
    ai_agent: AIAgent;
    constructor(agent: AIAgent) { /* ... */ }
    execute(): NodeResult {
        // 플레이어를 향해 이동 로직 수행
        if (this.ai_agent.isMovingToPlayer()) {
            return NodeResult.RUNNING;
        } else if (this.ai_agent.arrivedAtPlayer()) {
            return NodeResult.SUCCESS;
        }
        return NodeResult.FAILURE;
    }
}

// 특정 조건이 만족하는지 확인하는 리프 노드 (예: 플레이어가 사거리 내에 있는지)
class ConditionIsPlayerInRange implements BehaviorNode {
    ai_agent: AIAgent;
    constructor(agent: AIAgent) { /* ... */ }
    execute(): NodeResult {
        return this.ai_agent.isPlayerWithinAttackRange() ? NodeResult.SUCCESS : NodeResult.FAILURE;
    }
}

// 예시 AI Behavior Tree 구성 (서버에서 실행)
// AI가 플레이어를 발견하면 공격하고, 아니면 순찰한다.
const aiBehaviorTree = new SelectorNode([
    new SequenceNode([
        new ConditionIsPlayerInRange(myAIAgent),
        new ActionAttackPlayer(myAIAgent)
    ]),
    new ActionPatrol(myAIAgent)
]);

// 서버의 게임 루프에서 AI 틱마다 실행
// aiBehaviorTree.execute();
```

서버는 이 BT를 주기적으로 실행하여 AI의 현재 상태와 환경에 기반한 최적의 행동을 결정합니다. 여기까지는 좋습니다. 이제 이 '뇌'의 결정을 어떻게 전 세계에 흩어진 클라이언트들에게 지연 없이, 그리고 일관성 있게 전달할까요? 여기서 '신경계' 역할을 하는 네트워크 동기화 기법들이 등장합니다.

---

### **신경계의 마법: Dead Reckoning과 Rollback Sync의 융합**

서버에서 결정된 AI의 행동은 압축된 형태로 클라이언트에 전송됩니다. 클라이언트는 이 정보를 받아서 AI를 화면에 그려내야 합니다. 이때 단순히 서버 데이터를 기다리다간 AI가 뚝뚝 끊기거나 순간이동하는 것처럼 보이겠죠. 그래서 우리는 예측과 보정의 기술을 사용합니다.

#### **1. Dead Reckoning (추측 항법): 부드러운 예측**

Dead Reckoning은 클라이언트가 서버로부터 AI의 마지막 상태(위치, 속도, 현재 목표 행동 등)를 받은 후, 그 정보를 바탕으로 AI의 미래 위치를 자체적으로 '예측'하여 부드럽게 화면에 그려내는 기법입니다. 서버 업데이트가 도착하기 전까지 AI가 멈추지 않고 자연스럽게 움직이는 것처럼 보이게 하죠.

```pseudocode
// 클라이언트 측 AI 업데이트 로직 (간소화)
class ClientAIAgent {
    server_position: Vector3; // 서버로부터 마지막으로 받은 위치
    server_velocity: Vector3; // 서버로부터 마지막으로 받은 속도
    predicted_position: Vector3; // 클라이언트가 예측하는 현재 위치
    current_server_action_intent: string; // 서버가 현재 어떤 BT 액션을 수행하려는지 (예: "MoveToX", "AttackTarget")
    last_server_update_time: float;

    update(delta_time: float) {
        // 서버 업데이트가 도착했는지 확인
        if (new_server_data_available) {
            this.server_position = new_server_data.position;
            this.server_velocity = new_server_data.velocity;
            this.current_server_action_intent = new_server_data.action_intent;
            this.last_server_update_time = current_game_time;
        }

        // 현재 예측 위치를 서버의 '진실'에 가깝게 보정 (부드럽게 보간)
        // 오차 계산
        error_vector = this.server_position - this.predicted_position;
        // error_vector를 smooth_factor에 따라 predicted_position에 반영하여 보정
        this.predicted_position += error_vector * interpolation_factor * delta_time;

        // 서버의 현재 행동 의도(BT가 결정한)를 바탕으로 다음 위치를 예측
        switch (this.current_server_action_intent) {
            case "MoveToTarget":
                // 목표 지점을 향해 server_velocity로 예측 이동
                this.predicted_position += this.server_velocity * delta_time;
                break;
            case "Patrol":
                // 순찰 로직에 따라 예측 이동
                this.predicted_position += this.server_velocity * delta_time;
                break;
            // 기타 행동...
        }
    }
}
```

**Dead Reckoning의 강점:**
*   AI의 움직임을 시각적으로 매우 부드럽게 만듭니다.
*   네트워크 트래픽을 줄여줍니다 (서버가 모든 프레임 업데이트를 보낼 필요 없음).

하지만 Dead Reckoning만으로는 부족합니다. AI가 플레이어에게 피해를 입히거나, 환경과 상호작용하는 등 '결정적인' 행동을 할 때는 예측이 아닌 '확실한 진실'이 필요합니다. 여기서 Rollback Sync의 철학이 필요해집니다.

#### **2. Rollback Sync: 불변의 진실을 위한 되감기**

**Rollback Sync**는 주로 플레이어 간의 입력 지연을 보정하는 데 사용되지만, AI의 결정적인 행동에도 그 원리를 확장하여 적용할 수 있습니다. AI의 행동이 단순히 움직이는 것을 넘어, 게임 상태에 직접적인 영향을 미칠 때 (예: AI가 플레이어를 공격하여 체력을 깎는 경우) 클라이언트의 예측이 잘못되면 큰 문제가 됩니다.

**Rollback for AI:**
1.  **서버 권위**: 서버는 AI의 BT를 실행하여 어떤 행동을 언제 수행했는지 정확한 타임스탬프와 함께 기록합니다. 이 기록이 '진실'입니다.
2.  **클라이언트 예측 및 스냅샷**: 클라이언트는 Dead Reckoning으로 AI의 행동을 예측함과 동시에, 과거 몇 프레임 동안의 AI 상태 스냅샷을 저장해둡니다.
3.  **불일치 감지 및 롤백**: 서버로부터 AI의 확정된 과거 행동 상태가 도착했을 때, 클라이언트가 그 시점의 AI 상태를 저장된 스냅샷과 비교합니다. 만약 불일치(desync)가 발생했다면, 클라이언트는 해당 과거 시점으로 게임 상태를 '되감고(rollback)', 서버의 확정된 AI 상태를 적용한 다음, 현재 시점까지 게임을 '다시 시뮬레이션(resimulate)'합니다.

```pseudocode
// 클라이언트 측 AI Rollback (개념적 설명)
class ClientGameSimulation {
    ai_agents: Map<ID, ClientAIAgent>;
    historical_states: Map<float, GameStateSnapshot>; // 과거 게임 상태 스냅샷 저장

    process_server_update(server_ai_snapshot: ServerAIAgentSnapshot) {
        // 서버 스냅샷이 도착한 시간 (t_server)
        t_server = server_ai_snapshot.timestamp;

        // 클라이언트가 t_server 시점에 가졌던 AI 상태 스냅샷 로드
        client_ai_state_at_ts = this.historical_states.get(t_server).ai_agents[server_ai_snapshot.id];

        // 서버와 클라이언트의 AI 상태가 다른지 확인 (예: AI가 특정 행동을 했어야 하는데 클라이언트에선 안 했음)
        if (client_ai_state_at_ts.is_desynced_from(server_ai_snapshot)) {
            // 게임 시뮬레이션을 t_server 시점으로 되감기 (Rollback)
            this.rollback_to(t_server);

            // 서버의 권위 있는 AI 상태 적용
            this.ai_agents[server_ai_snapshot.id].apply_server_state(server_ai_snapshot);

            // t_server부터 현재까지 게임 다시 시뮬레이션 (Resimulate)
            this.resimulate_from(t_server, current_game_time);
        }
    }
}
```

**Rollback for AI의 장점:**
*   AI의 모든 행동에 대한 서버의 절대적인 권위를 보장합니다.
*   결정적인 상호작용(예: AI 공격의 히트 판정)에서 정확성과 공정성을 확보합니다.

물론, AI에 대한 완전한 롤백은 성능 비용이 매우 큽니다. 모든 AI의 상태를 저장하고 필요 시 되감는 것은 만만치 않은 일이죠. 그래서 보통은 `Dead Reckoning`을 기본으로 하되, AI의 핵심적인 행동이나 플레이어와 직접 상호작용하는 부분에 한해서 `Rollback` 원리를 적용하는 하이브리드 접근법을 사용합니다. 예를 들어, AI의 이동은 Dead Reckoning으로 예측하고, AI의 공격 판정은 서버가 최종적으로 확정하고 필요 시 롤백하는 식입니다.

---

### **아키텍처 스케치: 뇌와 신경계의 공조**

이러한 접근 방식의 이상적인 아키텍처는 다음과 같습니다.

1.  **서버 (AI의 뇌이자 진실의 원천):**
    *   모든 AI 엔티티의 Behavior Tree를 실행하여 AI의 의사결정을 합니다.
    *   AI의 현재 위치, 속도, 현재 수행 중인 BT 태스크 (예: "MoveTo(X,Y)", "Attack(PlayerID)")를 포함하는 최소한의 `AIStateUpdate` 패킷을 주기적으로 클라이언트에게 전송합니다.
    *   중요한 AI 액션 (예: 플레이어 타격, 아이템 획득)에 대한 확정적인 결과와 타임스탬프를 클라이언트에게 전송합니다.

2.  **클라이언트 (AI를 예측하고 그려내는 시각화 장치):**
    *   서버로부터 `AIStateUpdate`를 받으면, 이를 기반으로 `Dead Reckoning`을 사용하여 AI의 다음 움직임을 예측하고 화면에 부드럽게 렌더링합니다.
    *   서버의 확정적인 AI 액션 결과를 받으면, 자체 기록된 AI 상태와 비교합니다.
    *   만약 중요한 불일치가 발생하거나, 서버의 확정적인 액션이 클라이언트의 예측과 다를 경우, `Rollback` 원리를 사용하여 클라이언트의 AI 상태를 과거로 되감고, 서버의 진실을 적용한 후 현재까지 `Re-simulate`하여 시각적, 로직적 동기화를 맞춥니다.
    *   보간(Interpolation)을 사용하여 AI의 위치 변화를 더욱 부드럽게 만듭니다.

이러한 구조는 AI의 의사결정은 서버에서 권위 있게 처리하되, 클라이언트에서는 최소한의 정보로 AI의 행동을 최대한 자연스럽고 실시간처럼 보이도록 하는 효율적인 방법을 제공합니다.

---

### **시니어 개발자의 팁: 실전에서 부딪히며 배운 것들**

*   **BT와 시각화 분리**: Behavior Tree는 '무엇을 할 것인가'를 결정해야지, '어떻게 보일 것인가'를 결정해서는 안 됩니다. BT의 결과(예: 목표 지점, 대상)를 기반으로 클라이언트에서 애니메이션과 비주얼 효과를 처리해야 합니다.
*   **네트워크 최적화**: 모든 BT의 내부 상태를 네트워크로 보내는 것은 비효율적입니다. AI가 현재 실행 중인 가장 상위의 의미 있는 태스크나, 다음 목표 지점 같은 '결과물'만을 전송하세요.
*   **보정의 황금률**: Dead Reckoning 시 오차 보정은 너무 빠르면 AI가 뚝뚝 끊기고, 너무 느리면 랙처럼 보입니다. 게임의 특성과 AI의 중요도에 맞춰 `interpolation_factor`를 섬세하게 튜닝해야 합니다. 우리 팀은 AI 유형별로 다른 보정 계수를 사용했습니다.
*   **Rollback의 필요성 판단**: 모든 AI에 Full Rollback을 적용하는 것은 대부분 과잉 설계입니다. 플레이어에게 직접적인 영향을 미치거나, 게임의 핵심 로직과 강하게 엮인 AI(예: 보스 몬스터, PvP용 미니언)에만 전략적으로 Rollback을 적용하는 것을 고려하세요. 단순한 배경 AI는 Dead Reckoning으로 충분합니다.
*   **테스트 환경 구축**: 다양한 레이턴시 환경(0ms, 50ms, 100ms, 200ms 이상)을 시뮬레이션할 수 있는 테스트 도구를 반드시 만드세요. AI가 네트워크 환경에서 어떻게 동작하는지 정확히 파악하는 것이 중요합니다.

---

### **마무리하며: 뇌와 신경계의 하모니**

결국, 게임 AI 개발은 기술과 예술의 교차점이라고 생각합니다. `AI Behavior Trees`로 우리 AI에게 생생한 '지능'을 부여하고, `Server-Side Network Sync (Rollback/Dead Reckoning)`로 그 지능이 네트워크 지연 속에서도 끊김 없이 '생동감' 있게 전달되도록 하는 것이죠. AI의 뇌가 내린 의사결정이 네트워크라는 신경계를 타고 클라이언트의 눈에 실시간처럼 펼쳐질 때, 플레이어는 비로소 게임 속 세상에 완전히 몰입할 수 있을 겁니다.

이 두 가지 키워드의 결합은 단순히 기술적인 문제를 해결하는 것을 넘어, 네트워크 게임에서 AI의 역할을 한 단계 더 끌어올릴 수 있는 강력한 잠재력을 가지고 있습니다. 여러분의 다음 프로젝트에서 이 아이디어들이 멋진 영감이 되기를 바라며, 저는 또 다음 포스팅에서 더 깊이 있는 이야기로 찾아오겠습니다. 다들 즐거운 개발되세요!
