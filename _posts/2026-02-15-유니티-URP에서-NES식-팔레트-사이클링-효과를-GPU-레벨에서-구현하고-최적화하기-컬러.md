---
layout: post
title: "유니티 URP에서 NES식 팔레트 사이클링 효과를 GPU 레벨에서 구현하고 최적화하기: 컬러 룩업 텍스처와 버텍스 컬러를 활용한 마법같은 트릭"
categories: tech
---

### ⚠️ Image Generation Failed
```text
Prompt: A vibrant pixel art scene with animated water and fire, glowing with dynamic color shifts from a NES-era palette cycling effect, rendered with modern GPU precision, contrasting retro limitations with contemporary power, a quirky genius game developer's desk in the foreground, with code snippets floating around, illuminated by a neon sign that reads "Pixel Sorcery."
```

자, 여러분! 괴짜 천재 게임 개발자의 괴상한 블로그에 오신 것을 환영합니다! 오늘 우리는 시간 여행을 떠나 옛날 8비트 시대의 기발한 마법 중 하나를 훔쳐와, 우리 손안의 현대 GPU에 쑤셔 넣는 방법을 알아볼 겁니다. 주제는 바로 'NES식 팔레트 사이클링'!

**[재료 키워드]: Retro Console Architecture (NES/SNES/PS1), Unity Engine Tricks**

네, 맞아요. 그 삐끗거리는 듯하면서도 매혹적인 움직임! 마리오의 용암이 흐르고, 젤다의 물결이 일렁이던 그 효과 말입니다. "음, 그냥 GIF 애니메이션 쓰면 되는 거 아니에요?"라고 묻는 당신, 제 옆에 앉아 보세요. 당신의 코드가 CPU에서 팔레트 하나 바꾼다고 비명을 지르기 시작할 때, 제 이 마법 같은 트릭이 구원자가 될 겁니다. 단순히 애니메이션 텍스처를 쓰는 것보다 훨씬 더 '힙'하고, 훨씬 더 '최적화된' 방법이라구요!

---

### **1. NES의 눈물겨운(?) 팔레트 사이클링 마법, 그 실체는?**

옛날 NES는 참 불쌍하리만큼 자원이 부족했습니다. 화면에 동시에 표시할 수 있는 색상은 겨우 52색(배경 25색, 스프라이트 25색, 투명 2색)에 불과했죠. 그런데도 어떻게 용암이 꿀렁이고, 번개가 번쩍이는 것처럼 보였을까요? 개발자들은 잔머리를 굴렸습니다. 바로 **팔레트 메모리를 직접 조작**하는 거죠!

PPU(Picture Processing Unit)에 있는 팔레트 메모리는 특정 주소에 특정 색상 값을 저장하는 방식입니다. 예를 들어, 빨강, 주황, 노랑색이 순서대로 0번, 1번, 2번 팔레트 인덱스에 저장되어 있다고 가정해 봅시다. 여기서 0번 인덱스에 노랑을, 1번에 빨강을, 2번에 주황을 넣는 식으로 팔레트 인덱스의 색상 정보를 바꿔치기하면? 화면에 그려진 모든 픽셀은 여전히 "0번 팔레트의 색상을 써라!"라고 명령받지만, 그 0번 팔레트가 이제 노랑색이 되었으니, 갑자기 전체가 노랗게 변하는 겁니다. 이걸 눈 깜짝할 사이에 샥샥샥 바꿔주면, 마치 색상이 흐르는 듯한 착시 효과가 나타나는 거죠. CPU가 PPU의 팔레트 레지스터에 초당 60회씩 값을 때려 박는 겁니다. 으스스한 매력이 있죠?

### **2. 현대 유니티 URP에서 이 똥꼬쇼를 하는 이유? (성능 & 미학)**

물론 현대 GPU는 색상 표현에 제약이 없습니다. 모든 픽셀에 RGB 풀 컬러를 마음껏 뿌릴 수 있죠. 하지만 여전히 팔레트 사이클링은 유용합니다.

*   **미학**: 특정 레트로 스타일의 픽셀 아트 애니메이션에 이보다 더 좋은 건 없습니다. 물, 불, UI 요소의 강조 등, 의도된 레트로 감성을 표현하기에 최고죠.
*   **성능**: 복잡한 픽셀 아트 애니메이션을 매 프레임마다 새 텍스처로 그리는 대신, 단 하나의 정적인 텍스처를 사용하고 GPU에서 팔레트만 바꿔주면, 드로우 콜이나 VRAM 사용량을 크게 줄일 수 있습니다. 특히 모바일이나 저사양 기기에서 빛을 발하죠. "야, 그냥 GIF 쓰면 되지 왜?"라고 했던 친구, 이제 좀 이해가 가죠? 우리는 단순한 텍스처 스왑이 아니라, GPU의 연산 능력을 빌려 진정한 의미의 '색상 맵핑'을 구현할 겁니다.

### **3. '괴짜 천재'의 마법 같은 트릭: 컬러 룩업 텍스처(LUT)와 버텍스 컬러 인덱싱**

자, 본론입니다. 이 NES 팔레트 사이클링을 유니티에서 재현하는 가장 우아하고 효율적인 방법은 바로 **컬러 룩업 텍스처(LUT)**와 **GPU 셰이더**를 사용하는 겁니다.

**핵심 아이디어:**
1.  우리의 원본 픽셀 아트 텍스처는 실제 색상 값 대신, 미리 정의된 '팔레트 인덱스'를 담고 있습니다. (예: 0번은 빨강, 1번은 주황... 등)
2.  실제 색상 정보는 1D 텍스처(컬러 룩업 텍스처, 일명 팔레트 텍스처)에 저장합니다.
3.  셰이더에서 원본 텍스처의 픽셀을 읽어 인덱스를 추출하고, 그 인덱스를 가지고 팔레트 텍스처에서 최종 색상을 찾아 화면에 뿌립니다.
4.  애니메이션은? 팔레트 텍스처를 룩업할 때 사용하는 인덱스에 '오프셋'을 더하거나, 아예 팔레트 텍스처 자체의 내용을 동적으로 업데이트해 주는 방식으로 구현합니다.

#### **단계별 구현 가이드:**

**A. 텍스처 준비 (Preprocessing)**

이게 좀 귀찮지만, 한 번 해두면 두고두고 씁니다.
원본 픽셀 아트 이미지의 각 픽셀 색상을 팔레트 인덱스로 변환해야 합니다. 예를 들어, 이미지가 16색 팔레트를 사용한다면, 각 픽셀은 0부터 15까지의 인덱스 값을 가지게 됩니다. 이 인덱스 값은 RGBA 중 한 채널(예: R 채널)에 그레이스케일 값으로 저장하여 새로운 '인덱스 텍스처'를 만듭니다.

*   **예시**: 빨간색(#FF0000)을 0번 인덱스로, 주황색(#FFA500)을 1번 인덱스로 매핑하기로 했다면, 원본 텍스처의 빨간 픽셀은 R 채널에 0/255 (검정), 주황 픽셀은 1/255 (아주 어두운 회색) 값을 가지도록 저장합니다. 나머지 GBA 채널은 무시하거나 전부 1로 채웁니다.

**B. 팔레트 텍스처 (Lookup Texture) 준비**

이 녀석은 1D 텍스처가 가장 효율적입니다. 가로 픽셀 수가 우리의 팔레트 색상 개수와 같고, 높이는 1인 텍스처를 만듭니다. 여기에 실제 색상 값을 순서대로 넣어줍니다.

*   예시: 16색 팔레트라면, 폭 16, 높이 1인 텍스처를 만들고, 0번 픽셀에 첫 번째 팔레트 색상, 1번 픽셀에 두 번째 팔레트 색상... 이런 식으로 저장합니다.

**C. 셰이더 작성 (URP Shader Graph or HLSL)**

여기서 마법이 일어납니다! 우리는 `_MainTex` (인덱스 텍스처)와 `_PaletteTex` (팔레트 텍스처), 그리고 `_PaletteOffset` (애니메이션 오프셋)을 사용할 겁니다.

```hlsl
// URP Lit/Unlit Shader Graph - Custom Function 노드나 Custom Interpolator를 쓸 수도 있습니다.
// 여기서는 개념적인 HLSL Pseudo-code 입니다.

// Properties (Material Inspector에서 설정)
sampler2D _MainTex; // 원본 스프라이트 텍스처 (색상 대신 팔레트 인덱스를 저장)
sampler2D _PaletteTex; // 1D 팔레트 룩업 텍스처
float _PaletteSize; // 팔레트의 총 색상 개수
float _PaletteOffset; // 팔레트 애니메이션 오프셋 (시간에 따라 변경)
float _AnimationSpeed; // 애니메이션 속도

// Fragment Shader
fixed4 frag (v2f i) : SV_Target
{
    // 1. 원본 텍스처에서 픽셀 색상 (사실상 인덱스)을 가져옵니다.
    // R 채널에 인덱스 정보가 0-1 사이의 값으로 저장되어 있다고 가정합니다.
    float rawIndex = tex2D(_MainTex, i.uv).r;

    // 2. 이 인덱스를 실제 팔레트 인덱스(0 ~ _PaletteSize-1)로 변환합니다.
    float paletteIndex = rawIndex * (_PaletteSize - 0.0001); // 부동소수점 오차 방지

    // 3. 애니메이션 오프셋을 적용합니다.
    // _PaletteOffset은 C# 스크립트에서 시간에 따라 0, 1, 2... _PaletteSize-1 로 변경될 겁니다.
    // 모듈러 연산으로 인덱스가 팔레트 범위를 벗어나지 않도록 합니다.
    float finalIndex = fmod(paletteIndex + _PaletteOffset, _PaletteSize);

    // 4. 팔레트 텍스처에서 최종 색상을 가져옵니다.
    // 1D 텍스처이므로 U 좌표만 사용하고, V 좌표는 0.5로 고정합니다.
    // finalIndex를 [0, 1] 범위로 정규화합니다.
    fixed4 finalColor = tex2D(_PaletteTex, float2(finalIndex / _PaletteSize, 0.5));

    return finalColor;
}
```

**D. C# 스크립트 (애니메이션 제어)**

이제 셰이더의 `_PaletteOffset` 값을 시간에 따라 업데이트하여 팔레트 사이클링 애니메이션을 만듭니다.

```csharp
using UnityEngine;

public class PaletteCycler : MonoBehaviour
{
    public Material targetMaterial; // 셰이더가 적용된 Material
    public float cycleSpeed = 5.0f; // 초당 팔레트 인덱스 전환 속도
    public int paletteSize = 16; // 셰이더에 전달할 팔레트 크기 (중요!)

    private float _currentOffset = 0f;

    void Start()
    {
        if (targetMaterial == null)
        {
            Debug.LogError("대상 Material이 없습니다!", this);
            enabled = false;
            return;
        }
        targetMaterial.SetFloat("_PaletteSize", paletteSize);
    }

    void Update()
    {
        // 시간에 따라 오프셋을 증가시킵니다.
        _currentOffset += Time.deltaTime * cycleSpeed;

        // 팔레트 크기에 맞춰 오프셋을 래핑합니다.
        // float _currentOffset = (int)(Time.time * cycleSpeed) % paletteSize;
        // 이 방식으로 하면 정수 단위로 틱틱 끊기는 애니메이션을 만들 수 있습니다.
        // 부드러운 전환을 원하면 위처럼 float으로 계속 더하고 아래에서 fmod를 사용하세요.

        targetMaterial.SetFloat("_PaletteOffset", _currentOffset);
    }

    // 게임 오브젝트가 비활성화되거나 파괴될 때 재료를 정리하는 것이 좋습니다 (선택 사항).
    void OnDisable()
    {
        // _PaletteOffset 값을 초기화하거나, 재료 자체를 복제하여 사용하는 경우
        // 원본 재료에 영향을 주지 않도록 주의합니다.
    }
}
```

이 C# 스크립트를 스프라이트 렌더러가 붙은 게임 오브젝트에 추가하고, 셰이더가 적용된 Material을 할당해주면 끝!

### **4. 더 나아가기: 버텍스 컬러를 이용한 팔레트 인덱싱 (고급 트릭)**

만약 스프라이트 전체가 아닌, 오브젝트의 특정 부분만 팔레트 사이클링을 하고 싶다면? 예를 들어, 캐릭터가 들고 있는 불꽃만 깜빡이게 하고 싶다거나. 이때는 버텍스 컬러를 활용할 수 있습니다.

**아이디어**: 메시의 버텍스 컬러(RGBA) 중 하나의 채널(예: R 채널)에 해당 버텍스나 폴리곤이 사용할 '기본 팔레트 인덱스'를 저장하는 겁니다. 셰이더에서 `rawIndex`를 `tex2D(_MainTex, i.uv).r` 대신 `i.color.r`로 가져오면 되겠죠. 이렇게 하면 텍스처가 공유되어도 버텍스별로 다른 팔레트 인덱스를 가질 수 있게 됩니다. 메시를 직접 편집해서 버텍스 컬러를 조작해야 하니 좀 더 수고롭지만, 더욱 세밀한 제어가 가능해집니다.

```hlsl
// Fragment Shader (Vertex Color Indexing 버전)
fixed4 frag (v2f i) : SV_Target
{
    // i.color.r 에 팔레트 인덱스가 0-1 범위로 저장되어 있다고 가정
    float rawIndex = i.color.r;

    // 이하 팔레트 텍스처 샘플링 로직은 동일
    float paletteIndex = rawIndex * (_PaletteSize - 0.0001);
    float finalIndex = fmod(paletteIndex + _PaletteOffset, _PaletteSize);
    fixed4 finalColor = tex2D(_PaletteTex, float2(finalIndex / _PaletteSize, 0.5));

    return finalColor;
}
```

### **5. 괴짜 천재의 잔소리 & 팁**

*   **성능**: 이 방법은 CPU 오버헤드가 거의 없고, GPU에서 텍스처 룩업 한두 번만 더 하면 되므로 매우 빠릅니다. 특히 여러 오브젝트가 같은 셰이더와 팔레트 텍스처를 공유한다면, 드로우 콜 배치(Batching)에도 유리합니다.
*   **유연성**: `_PaletteTex` 자체를 C# 스크립트에서 `Texture2D.SetPixels()`로 동적으로 변경하면, 팔레트 색상 자체를 실시간으로 바꿀 수도 있습니다. 낮밤 주기, 독 상태, 광기 상태 등 게임 상태에 따라 팔레트 분위기를 완전히 바꿔버리는 것도 가능하죠. (물론 이 경우 `SetPixels`는 CPU 작업이므로 `Apply()` 전에 미리 계산해두는 게 좋습니다)
*   **URP Shader Graph**: URP 사용자라면 Shader Graph에서 Custom Function 노드를 사용하거나, Sub Graph로 위 로직을 구현하는 것이 훨씬 시각적이고 편리합니다. `Sampler 2D` 노드와 `Multiply`, `Fraction`, `Add` 등의 노드를 조합하면 HLSL 코드를 직접 안 짜도 됩니다!
*   **"왜 이렇게까지 해야 해?"**: "게임을 만드는데 이렇게까지 꼼꼼해야 하나요?"라고 묻는 당신, 갓겜은 사소한 디테일에서 태어납니다. 그리고 때로는 이런 '꼼수'들이 게임의 퍼포먼스와 예술적 표현의 한계를 돌파하는 열쇠가 되죠. NES 개발자들이 그랬던 것처럼!

---

자, 여기까지입니다! 옛날 NES 할아버지가 쓰던 낡은 트릭이 현대 유니티 URP에서도 이렇게 멋지게 부활할 수 있다니, 신기하지 않나요? 이 기술을 잘 활용해서 여러분만의 독창적인 픽셀 아트 애니메이션을 만들어보세요. 다음에 또 다른 괴상하고도 유용한 개발 꼼수로 찾아오겠습니다! 뿅!
