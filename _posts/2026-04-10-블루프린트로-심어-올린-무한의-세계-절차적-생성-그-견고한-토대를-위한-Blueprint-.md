---
layout: post
title: "블루프린트로 심어 올린 무한의 세계: 절차적 생성, 그 견고한 토대를 위한 Blueprint Best Practices"
categories: tech
---

### ⚠️ Image Generation Failed
```text
Prompt: Abstract futuristic architectural blueprint glowing with energy, intricate procedural generation patterns forming infinite landscapes, cinematic lighting, deep blue and orange hues, high detail, digital art, artstation.
```

안녕하세요, 게임 개발의 황무지를 오랫동안 헤쳐온 시니어 개발자이자 가끔은 키보드 워리어로 변신하는 테크 블로거입니다. 오늘은 **Unreal Engine Blueprints Best Practices**와 **Procedural Generation Algorithms**라는, 언뜻 보면 조금 동떨어져 보일 수도 있는 두 개의 키워드를 엮어보려 합니다. 하지만 저의 오랜 경험상, 이 둘은 떼려야 뗄 수 없는 관계에 있습니다. 마치 훌륭한 요리사가 아무리 좋은 재료를 써도 조리 도구가 엉망이면 제대로 된 음식을 만들 수 없듯이, 절차적 생성 알고리즘의 무한한 잠재력도 견고하고 잘 정돈된 블루프린트 위에서 비로소 빛을 발할 수 있기 때문이죠.

### 문제는 늘 '성장통'에서 시작됩니다

처음 게임을 만들 때, 블루프린트는 정말 축복 같았습니다. C++ 없이도 눈 깜짝할 사이에 복잡한 로직을 구현할 수 있었으니까요. 특히 초기 절차적 생성 시스템을 만들 때는요. 노이즈 함수를 연결하고, 메시를 생성하고, 오브젝트를 스폰하는 과정이 너무나 쉽고 직관적이었죠.

**하지만 문제는 늘 프로젝트의 '성장통'과 함께 찾아옵니다.**
1.  **스파게티 블루프린트의 저주:** 처음엔 단순했던 절차적 생성 로직이 점점 복잡해지면서, 하나의 블루프린트 안에 수백 개의 노드가 엉켜버리는 일이 비일비재했습니다. 디버깅은 미로 찾기가 되고, 기능 하나 추가하려면 기존 로직을 통째로 이해해야 하는 지경에 이르렀죠.
2.  **재사용성의 부재:** 특정 월드 생성 로직이 다른 곳에서 필요할 때, 해당 부분을 복사-붙여넣기 하거나 아예 새로 만드는 악순환이 반복되었습니다. 결국, 일관성 없는 로직들이 여기저기 흩어져 관리 비용만 늘어났죠.
3.  **성능 저하의 함정:** 절차적 생성은 필연적으로 많은 연산을 요구합니다. 그런데 블루프린트에서 비효율적인 루프나 불필요한 연산이 많아지면, 게임이 버벅거리거나 아예 멈추는 불상사가 발생했습니다. 특히 '틱' 이벤트에서 무거운 연산을 돌리는 건 자살행위와 같죠.

저도 한때 이런 문제들로 밤샘 야근을 밥 먹듯 했습니다. 하지만 이 경험을 통해 블루프린트도 C++ 코드처럼 '설계'의 중요성을 깨달았습니다. 그리고 절차적 생성 시스템에 적용할 수 있는 몇 가지 '모범 사례'를 정립하게 되었죠.

### 절차적 생성, 블루프린트 위에 견고한 성을 쌓다

저는 블루프린트를 단순한 비주얼 스크립팅 도구가 아니라, '설계도가 시각화된 소프트웨어'라고 생각합니다. 따라서 C++ 개발에서 사용하는 여러 원칙들을 블루프린트에도 적용해야 한다고 굳게 믿습니다. 특히 절차적 생성 시스템을 만들 때는요!

#### 1. 명확한 책임 분리: '역할'을 부여하라 (Function, Macro, Custom Component)

**문제:** 모든 절차적 생성 로직이 하나의 액터 블루프린트 안에 응집되어 거대한 단일체(Monolithic)를 이룹니다. 어디서부터 손대야 할지 감이 안 잡힙니다.

**해결:** 생성 과정을 작고 명확한 단위로 쪼개세요.

*   **함수 (Functions):** 특정 작업을 수행하는 순수한(Pure) 또는 비순수한(Impure) 함수를 만드세요. 예를 들어 `CalculateNoiseValue`, `GetSpawnLocation`, `GenerateMeshSection` 등. 특히 'Pure' 함수는 side-effect가 없어 디버깅이 훨씬 쉽습니다.
*   **매크로 (Macros):** 반복적으로 사용되는 노드 시퀀스를 묶어서 재사용성을 높이세요. 예를 들어 `ClampToGrid`, `RandomPointInRadius` 같은 유틸리티성 로직에 적합합니다.
*   **사용자 정의 컴포넌트 (Custom Actor Components):** 월드 생성, 지형 생성, 오브젝트 스폰 등, 각기 다른 '책임'을 가진 로직은 독립적인 액터 컴포넌트로 분리하세요.

**의사 코드/로직 예시:**

```
// BP_WorldGenerator Actor
Event BeginPlay -> GenerateWorld

Function GenerateWorld:
    Create New Component (ProceduralTerrainGeneratorComponent)
    Call TerrainGeneratorComponent->GenerateTerrain(WorldSettings)
    Bind TerrainGeneratorComponent->OnTerrainGenerated Event to OnTerrainDone
    // 비동기 처리를 위해 기다림

Event OnTerrainDone:
    Create New Component (ProceduralObjectSpawnerComponent)
    Call ObjectSpawnerComponent->SpawnObjects(TerrainData, ObjectSettings)
    Bind ObjectSpawnerComponent->OnObjectsSpawned Event to OnObjectsDone

// UProceduralTerrainGeneratorComponent
Function GenerateTerrain(WorldSettings):
    For X from 0 to SizeX:
        For Y from 0 to SizeY:
            NoiseValue = Call BlueprintFunctionLibrary->GetPerlinNoise(X, Y, Seed)
            Height = NoiseValue * WorldSettings.HeightScale
            SetTerrainHeight(X, Y, Height)
    Fire OnTerrainGenerated Event (with TerrainData)
```

이처럼 컴포넌트로 나누면 각 컴포넌트의 역할이 명확해지고, 나중에 지형 생성 알고리즘만 바꾸고 싶을 때 `ProceduralTerrainGeneratorComponent`만 교체하거나 수정하면 됩니다.

#### 2. 데이터 기반 설계: '설정'과 '로직'을 분리하라 (Data Assets, Structs)

**문제:** 노이즈 스케일, 오브젝트 스폰 확률, 몬스터 타입 등 모든 설정값이 블루프린트 그래프 안에 하드코딩되어 있습니다. 값을 바꾸려면 그래프를 열고 노드를 찾아야 합니다.

**해결:** 모든 동적인 설정값은 데이터 에셋(Data Assets)과 구조체(Structs)를 활용하여 로직과 분리하세요.

*   **데이터 에셋 (UDataAsset):** 특정 바이옴의 설정값(`UBiomeSettingsDataAsset`), 노이즈 파라미터(`UNoiseProfileDataAsset`), 스폰될 오브젝트 목록(`UObjectSpawnTableDataAsset`) 등을 정의하고 에셋으로 만들어 사용합니다. 디자이너가 코드 없이도 다양한 월드를 만들 수 있게 됩니다.
*   **구조체 (Structs):** 관련 있는 데이터를 묶어서 깔끔하게 관리합니다. 예를 들어 `FWorldGenerationParameters`는 `Seed`, `Scale`, `Octaves` 등을 포함할 수 있습니다.

**예시:**

```
// UBiomeSettingsDataAsset (Data Asset)
Variables:
    NoiseProfile (UNoiseProfileDataAsset Reference)
    SpawnTables (Array of UObjectSpawnTableDataAsset References)
    TerrainMaterial (Material Instance)
    MaxHeight (Float)
    ...

// FWorldGenerationParameters (Struct)
Variables:
    Seed (Integer)
    WorldScale (Vector)
    BiomeType (EBiomeEnum)
    ...

// BP_WorldGenerator에서 사용:
Get Current Biome Data Asset -> Get Noise Profile -> Apply to Noise Generator
Get Current Biome Data Asset -> Get Spawn Tables -> Pass to Object Spawner
```

이렇게 하면, 새로운 바이옴을 만들거나 기존 바이옴의 설정을 변경할 때, 블루프린트 로직은 건드리지 않고 데이터 에셋만 수정하면 됩니다. 이터레이션 속도가 비약적으로 빨라지죠.

#### 3. 이벤트 기반 아키텍처: '흐름'을 제어하라 (Event Dispatchers, Latent Actions)

**문제:** 지형 생성이 완료되기 전에 오브젝트 스폰 로직이 시작되거나, 모든 생성이 끝나기 전에 플레이어가 월드에 진입하는 등, 순서 문제가 발생합니다. 무거운 생성 과정이 메인 스레드를 막아 게임이 멈춥니다.

**해결:** 이벤트 디스패처와 Latent Actions를 활용하여 비동기적이고 순차적인 흐름을 만드세요.

*   **이벤트 디스패처 (Event Dispatchers):** 특정 작업이 완료되었음을 알리는 신호등 역할을 합니다. `OnTerrainGenerationComplete`, `OnObjectsPlaced` 등의 이벤트를 바인딩하여 다음 단계를 시작합니다.
*   **Latent Actions (비동기 노드):** 시간이 오래 걸리는 작업 (예: 메시 생성, 애셋 로드)을 백그라운드에서 처리하고, 완료되면 다음 노드로 넘어가는 기능을 제공합니다. `Delay`, `Set Timer by Event` 등이 대표적이죠. C++에서 비동기 작업을 처리하고 그 결과를 블루프린트로 다시 돌려줄 때도 유용합니다.

**로직 예시:**

```
// BP_WorldGenerator
Event GenerateWorld:
    // 지형 생성 컴포넌트에 생성 시작을 알림
    Call ProceduralTerrainGeneratorComponent->StartGeneration(Params)
    // 지형 생성이 완료되면 OnTerrainGenerated 이벤트가 발동하도록 바인딩
    Bind Event to OnTerrainGeneratedEvent (Custom Event)

Custom Event OnTerrainGeneratedEvent(GeneratedTerrainData):
    // 지형 생성이 완료되었으니, 이제 오브젝트 스폰 컴포넌트에 스폰 시작을 알림
    Call ProceduralObjectSpawnerComponent->StartSpawn(GeneratedTerrainData, SpawnTable)
    // 오브젝트 스폰 완료 이벤트를 바인딩
    Bind Event to OnObjectsSpawnedEvent (Custom Event)

// UProceduralTerrainGeneratorComponent 내부:
Function StartGeneration(Params):
    // ... 무거운 지형 생성 로직 ...
    // 생성 완료 후 이벤트 발동
    Call OnTerrainGeneratedEvent.Broadcast(TerrainData)
```

이런 방식으로 파이프라인을 구성하면, 각 단계가 독립적으로 작동하면서도 순서가 보장되고, UI가 멈추는 현상을 최소화할 수 있습니다.

#### 4. 성능 의식적인 개발: '블루프린트의 한계'를 인지하라 (Profiler, Caching, C++ 연동)

**문제:** "블루프린트는 느려!"라는 편견을 스스로 증명해버립니다. 복잡한 계산이나 대규모 데이터 처리에 무턱대고 블루프린트를 사용하면 프레임이 뚝뚝 떨어집니다.

**해결:** 블루프린트의 강점과 약점을 이해하고, 성능을 최적화하는 전략을 세우세요.

*   **프로파일러 활용:** 언리얼 엔진의 프로파일러(Console Command: `stat unit`, `stat gameplay`, `profilegpu`)를 통해 어디서 병목 현상이 발생하는지 정확히 파악하세요. 감으로 때려잡는 건 금물입니다.
*   **캐싱 (Caching):** 자주 접근하는 계산 결과나 데이터는 변수에 저장해두고 재사용하세요. 매번 다시 계산하거나 `GetAllActorsOfClass` 같은 비싼 함수를 호출하지 마세요.
*   **효율적인 루프:** `For Each Loop`는 편리하지만, 내부 로직이 무거울 경우 성능에 영향을 줍니다. 정말 필요한 경우에만 사용하고, 가능하면 더 최적화된 방법 (예: 특정 공간 영역만 처리)을 고민하세요.
*   **C++ 연동의 마법:** 복잡한 수학 연산, 대규모 배열 처리, 멀티스레딩이 필요한 부분은 과감히 C++로 내려가 구현하고, 이를 블루프린트에 노출(BlueprintCallable)하여 사용하세요. 블루프린트는 '오케스트레이션'에 강하고, C++는 '계산'에 강하다는 것을 잊지 마세요.

**저의 경험담:** 한 번은 복잡한 노이즈 함수를 블루프린트로만 구현하려다가 게임이 완전히 멈춘 적이 있습니다. 결국 핵심 노이즈 계산 로직은 C++로 포팅하고, 그 결과를 블루프린트에서 매개변수만 바꿔가며 호출하는 방식으로 변경했더니, 거짓말처럼 쾌적해졌죠. 블루프린트는 '어떻게 생성할지'를 지시하는 사령관이고, C++는 '실제로 생성하는' 빠르고 숙련된 일꾼이라고 생각하시면 편합니다.

#### 5. 가독성, 주석, 네이밍 컨벤션: '협업'을 위한 기본기 (Comments, Reroute Nodes, Naming Conventions)

**문제:** 어제 만든 블루프린트도 오늘 보면 내가 이걸 왜 이렇게 만들었지? 하는 의문이 듭니다. 다른 개발자는 아예 손대기도 두려워합니다.

**해결:** 팀원들과의 협업, 그리고 미래의 나 자신을 위해 명확한 규칙을 만드세요.

*   **주석 (Comments):** 복잡한 로직이나 의도가 불분명한 노드 묶음에는 반드시 주석 박스를 달아 설명하세요. '이 부분은 퍼린 노이즈를 이용한 지형 높이 계산 로직' 처럼요.
*   **리라우트 노드 (Reroute Nodes):** 선이 엉키는 것을 방지하고, 그래프를 깔끔하게 정리하는 데 사용합니다. 마치 전선 정리를 하는 것과 같습니다.
*   **네이밍 컨벤션 (Naming Conventions):** 변수, 함수, 이벤트, 블루프린트 파일명 등에 일관된 규칙을 적용하세요 (예: `BP_Player`, `F_CalculateDamage`, `EV_OnDeath`). `BPI_` (Blueprint Interface), `PF_` (Pure Function), `MF_` (Macro Function) 등 접두사를 활용하는 것도 좋습니다.

이런 기본적인 규칙들은 당장 개발 속도를 빠르게 하지는 않지만, 장기적으로 프로젝트의 유지보수성과 확장성에 지대한 영향을 미칩니다. 결국 개발자의 삶의 질을 높여주는 지름길이죠.

### 결론: 블루프린트는 그저 도구가 아니다

우리는 게임 개발자입니다. 새로운 세상을 창조하는 사람들이죠. 그리고 그 세상을 무한히 넓히고 다채롭게 만드는 절차적 생성 알고리즘은 현대 게임 개발에서 빼놓을 수 없는 핵심 기술입니다. 하지만 아무리 멋진 알고리즘이라도, 그것을 담아낼 그릇, 즉 블루프린트가 엉망이라면 그 잠재력을 온전히 발휘하기 어렵습니다.

**Unreal Engine Blueprints Best Practices**는 단순히 깔끔한 그래프를 만드는 것을 넘어, 여러분의 **Procedural Generation Algorithms**가 더욱 견고하고, 확장 가능하며, 성능 좋은 시스템으로 거듭날 수 있는 토대를 제공합니다. 블루프린트를 그저 '빠르게 뭔가 만드는 도구'로 치부하지 마세요. C++ 코드와 마찬가지로, 체계적인 설계와 원칙을 적용하면 강력한 '소프트웨어 설계 도구'가 될 수 있습니다.

오늘 제가 이야기한 내용들이 여러분의 절차적 생성 프로젝트를 한 단계 업그레이드하는 데 작은 영감이 되기를 바랍니다. 블루프린트 위에서 무한한 세계를 만들어나갈 여러분의 도전을 응원합니다!
