---
layout: post
title: "멈춤 없는 성능! Shader Math & GLSL로 Unity 렌더링 최적화의 심장을 뛰게 하는 비결"
categories: tech
---

### ⚠️ Image Generation Failed
```text
Prompt: A highly abstract, digital art representation of intertwined concepts: glowing lines of GLSL code forming complex mathematical fractals, converging onto a stylized Unity Engine logo. Dynamic, cinematic lighting with deep blues, purples, and electric greens. Energy flows from the code to the engine icon, symbolizing optimization. High detail, ethereal glow, deep focus.
```

안녕하세요, 동료 개발자 여러분! 오늘 커피 한 잔 들고 나누고 싶은 이야기는 바로 'Shader Math & GLSL' 그리고 이 지식이 'Unity Engine Optimization Tricks'와 어떻게 강력하게 결합되어 여러분의 게임을 한 단계 더 끌어올릴 수 있는지에 대한 심도 있는 탐구입니다. 단순히 멋진 비주얼을 만드는 것을 넘어, 이 멋짐을 극한의 효율로 구현하는 비법을 함께 파헤쳐 봅시다.

### 1. "아, 이 비싼 녀석!" – 복잡한 비주얼이 불러온 GPU의 비명

여러분도 이런 경험 있으실 겁니다. 기획자가 "물결이 찰랑거리는 신비로운 마법 장막 효과"나 "벽을 녹여버리는 듯한 파괴적인 디졸브 효과"를 원해서 열심히 텍스처 여러 장 붙이고, 알파 블렌딩 덕지덕지 바르고, C#에서 매 프레임 파라미터 업데이트해서 구현했더니... GPU 프로파일러가 빨간 불을 뿜어내며 비명을 지르는 상황 말이죠. 특히 모바일이나 VR 같은 자원 제약이 심한 플랫폼에서는 이런 작은 비효율들이 프로젝트의 성패를 좌우하기도 합니다.

**문제의 핵심**:
1.  **과도한 텍스처 샘플링**: 복잡한 패턴이나 노이즈를 위해 여러 개의 텍스처를 샘플링하는 것은 GPU 캐시 미스를 유발하고 메모리 대역폭을 잡아먹습니다.
2.  **높은 오버드로(Overdraw)**: 투명한 오브젝트들이 겹쳐서 그려질수록 픽셀 셰이더가 여러 번 실행되어 GPU 부하가 급증합니다.
3.  **CPU-GPU 간 비효율적인 통신**: C# 스크립트에서 매 프레임 수많은 Material Property를 업데이트하는 것은 CPU-GPU 동기화 병목을 일으킬 수 있습니다.

이런 문제들은 결국 프레임 드랍으로 이어지고, 플레이어의 몰입감을 해치는 주범이 됩니다. 이쯤 되면 '도대체 어떻게 하면 이 비주얼을 유지하면서도 성능을 챙길 수 있을까?' 하는 깊은 고민에 빠지게 되죠.

### 2. GPU, 너의 언어는 수학이다! – Shader Math의 재발견

여기서 우리의 구원투수가 등장합니다: 바로 **Shader Math & GLSL**입니다. GPU는 병렬 연산에 특화된 괴물 같은 계산기입니다. 그리고 이 계산기가 가장 효율적으로 이해하고 처리하는 언어는 다름 아닌 **수학**입니다. 텍스처를 읽어와서 계산하는 대신, 순수한 수학 연산으로 픽셀의 색상이나 위치를 결정하게 하면, 우리는 훨씬 적은 비용으로 놀라운 효과를 만들어낼 수 있습니다.

"근데 GLSL은 Unity에서 직접 안 쓰지 않나요? HLSL/CG 아닌가요?"라고 생각하실 수 있습니다. 맞습니다, Unity는 내부적으로 HLSL이나 Cg를 컴파일하지만, 셰이더의 핵심 수학 로직은 언어에 크게 구애받지 않습니다. GLSL의 사고방식과 함수들은 다른 셰이더 언어에도 통용되는 보편적인 개념입니다. 실제로 Shader Graph 같은 비주얼 셰이더 에디터도 결국 내부적으로는 이 수학적 노드들을 조합하여 코드를 생성하는 것이죠.

**해결책의 핵심**:
*   **텍스처 대신 수학으로 패턴 생성**: 노이즈 함수(Perlin, Worley 등), 삼각 함수(sin, cos), Step/Smoothstep 함수, 벡터 연산 등을 활용하여 복잡한 패턴을 텍스처 없이 직접 생성합니다.
*   **GPU에서 직접 애니메이션**: 시간(time) 변수를 활용하여 모든 애니메이션 로직을 GPU에서 처리하여 CPU의 부담을 덜어줍니다.
*   **정교한 공간 연산**: 오브젝트의 로컬/월드 공간 정보와 카메라/라이트 정보를 활용하여 더욱 정교하고 효율적인 렌더링 로직을 구축합니다.

### 3. Deep Dive: "텍스처 없는 동적 노이즈 마스크"로 디졸브 효과 최적화

자, 구체적인 예시로 들어가 봅시다. 위에서 언급했던 "벽을 녹이는 듯한 디졸브 효과"를 상상해 보세요. 일반적인 접근은 노이즈 텍스처를 샘플링하고, 디졸브 정도를 나타내는 `_DissolveThreshold` 값과 비교하여 `smoothstep` 함수로 부드러운 전환을 만드는 것입니다.

**기존 방식의 문제**:
1.  노이즈 텍스처를 메모리에 로드하고 매 픽셀마다 샘플링해야 합니다 (텍스처 메모리, 샘플링 비용).
2.  노이즈 패턴을 바꾸려면 새로운 텍스처가 필요합니다.

**Shader Math & GLSL을 이용한 최적화**:
우리는 텍스처 없이, 순수하게 셰이더 수학으로 동적인 노이즈를 생성하고 이를 마스크로 활용할 수 있습니다. 예를 들어, 심플한 "Value Noise"나 "Worley Noise"를 셰이더에서 직접 구현하는 것이죠.

**핵심 알고리즘 로직 (Pseudo-code / GLSL-ish)**:

```glsl
// GLSL-ish Pseudocode for a simple 2D Value Noise function
float hash(vec2 p) {
    // 임의의 숫자를 생성하는 해싱 함수 (간단하게 구현)
    // 수학적으로 의미 있는 "난수"를 생성하여 패턴을 만듭니다.
    return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453);
}

float noise(vec2 p) {
    vec2 i = floor(p); // 정수 부분
    vec2 f = fract(p); // 소수 부분 (0~1)

    // 코너 값 해싱
    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));

    // 부드러운 보간 (Quadratic Hermite curve)
    vec2 u = f * f * (3.0 - 2.0 * f); // smoothstep 대신 수동 구현

    // 선형 보간 (Lerp)
    return mix(mix(a, b, u.x),
               mix(c, d, u.x),
               u.y);
}

// 픽셀 셰이더 메인 함수 예시
void frag(out half4 outColor : SV_Target, float2 uv : TEXCOORD0, float _DissolveThreshold) {
    float time = _Time.y * 0.5; // Unity의 시간 변수 활용
    vec2 noiseUV = uv * 5.0 + vec2(time, time); // 노이즈 스케일 및 애니메이션

    float dynamicNoise = noise(noiseUV); // 순수 수학으로 노이즈 생성

    // 디졸브 임계값과 비교하여 마스크 생성
    float mask = smoothstep(_DissolveThreshold - 0.1, _DissolveThreshold + 0.1, dynamicNoise);

    // 디졸브 효과 적용 (예시: 빨간색으로 녹는 효과)
    half4 baseColor = half4(1.0, 1.0, 1.0, 1.0); // 원래 오브젝트 색상
    half4 dissolveColor = half4(1.0, 0.0, 0.0, 1.0); // 녹는 부분 색상

    outColor = mix(baseColor, dissolveColor, mask);
    outColor.a = 1.0 - mask; // 녹는 부분 투명하게 처리 (오버드로 주의)
}
```
**코드 설명**:
1.  `hash` 함수는 입력 `vec2` 값에 따라 예측 가능한 의사 난수를 생성합니다. 이 함수의 '매직 넘버'들은 시각적으로 좋은 패턴을 만들기 위해 튜닝됩니다.
2.  `noise` 함수는 `hash` 함수를 사용하여 그리드 코너의 값을 계산하고, `f` (소수 부분)를 사용하여 이 값들을 부드럽게 보간(Interpolation)하여 최종 노이즈 값을 만듭니다. `u` 변수는 `smoothstep`과 유사하게 0에서 1까지 부드럽게 변하는 보간 계수를 만듭니다.
3.  `frag` 함수에서는 UV와 `_Time`을 사용하여 노이즈를 동적으로 생성하고, `_DissolveThreshold` 값과 `smoothstep`으로 마스크를 만듭니다.

이 방식의 장점은 명확합니다.
*   **텍스처 로딩 및 샘플링 비용이 0!** : GPU 메모리 사용량을 줄이고, 텍스처 캐시 미스를 원천적으로 방지합니다.
*   **무한한 다양성**: 노이즈 함수의 파라미터를 조절하거나, 여러 노이즈 함수를 조합하여 (Fractal Brownian Motion 등) 무궁무진한 패턴을 만들 수 있습니다. 텍스처를 다시 그릴 필요가 없습니다.
*   **완벽한 프로시저럴 애니메이션**: `_Time` 변수만으로 복잡한 노이즈의 흐름이나 변화를 GPU에서 효율적으로 제어합니다.

### 4. Unity Engine Optimization Tricks: 실전 적용 가이드

이제 이 최적화된 셰이더 수학을 Unity에서 어떻게 효과적으로 활용하여 궁극적인 최적화를 이룰 수 있는지 몇 가지 실전 팁을 드립니다.

1.  **Scriptable Render Pipeline (SRP) 커스텀 패스 활용**:
    HDRP나 URP 같은 SRP를 사용한다면, 커스텀 Render Feature를 만들어서 특정 렌더링 단계에서 이 프로시저럴 셰이더를 실행할 수 있습니다. 예를 들어, 포스트 프로세싱 단계에서 스크린 공간 노이즈를 생성하거나, 특정 오브젝트에만 적용되는 패스를 만들어 불필요한 연산을 줄일 수 있습니다. 이는 렌더링 파이프라인에 대한 정교한 제어를 가능하게 하여, 최적화된 셰이더의 진가를 발휘할 수 있게 합니다.

2.  **MaterialPropertyBlock으로 드로우콜 최소화**:
    같은 셰이더를 사용하지만 파라미터만 다른 여러 오브젝트가 있다면, 매번 새로운 `Material` 인스턴스를 만들지 마세요. `MaterialPropertyBlock`을 사용하면 런타임에 드로우콜을 합치면서도 각 오브젝트에 고유한 파라미터(예: `_DissolveThreshold`, 노이즈 스케일)를 전달할 수 있습니다. 이는 특히 대량의 오브젝트에 동적 효과를 적용할 때 드로우콜 최적화에 큰 도움이 됩니다.

    ```csharp
    // C# 코드 예시
    MaterialPropertyBlock _mpb;

    void Start() {
        _mpb = new MaterialPropertyBlock();
    }

    void Update() {
        // 오브젝트마다 다른 임계값 적용
        float dissolveProgress = Mathf.Lerp(0f, 1f, Time.time / 10f);
        _mpb.SetFloat("_DissolveThreshold", dissolveProgress);

        // 렌더러에 MaterialPropertyBlock 적용
        GetComponent<Renderer>().SetPropertyBlock(_mpb);
    }
    ```

3.  **Shader Variants와 Keyword 관리**:
    하나의 셰이더 파일에 여러 기능을 넣다 보면 `if`문이나 복잡한 `#ifdef` 지시어로 코드가 비대해질 수 있습니다. Unity는 `Shader Keyword`를 사용하여 필요한 기능만 포함하는 셰이더 베리언트를 컴파일하고 런타임에 스위칭할 수 있습니다. 사용하지 않는 기능은 컴파일하지 않음으로써 셰이더 로딩 시간과 메모리 사용량을 줄일 수 있습니다. "이 효과는 특정 상황에서만 필요한데..." 싶을 때 아주 유용하죠.

4.  **Unity Profiler 및 Frame Debugger 적극 활용**:
    최적화의 황금률은 "측정 없이는 최적화도 없다"입니다. Unity Profiler의 GPU 섹션과 Frame Debugger를 통해 여러분이 만든 셰이더가 실제로 얼마나 효율적으로 작동하는지, 어떤 패스에서 병목이 발생하는지 정확하게 파악해야 합니다. 텍스처 샘플링 비용이 줄었는지, 픽셀 셰이더 연산 시간이 단축되었는지 눈으로 확인하세요. 숫자만이 진실을 말해줍니다!

### 5. 나의 경험담: "수학이 나를 구원했다!"

저는 한때 모바일 VR 프로젝트에서 과도한 노이즈 텍스처 샘플링과 복잡한 블렌딩으로 인해 프레임이 심하게 떨어지는 문제에 직면한 적이 있습니다. 당시 팀은 "텍스처 해상도를 낮추거나, 아예 효과를 빼자"는 의견이 지배적이었죠. 하지만 저는 '이대로 포기할 수 없다!'는 마음으로 며칠 밤낮을 GLSL 관련 자료를 파고들었습니다.

그리고 제가 발견한 것이 바로 셰이더 내에서 Perlin Noise를 직접 구현하는 방식이었습니다. 처음에는 코드가 너무 길어지고 복잡해져서 걱정했지만, 막상 적용하고 프로파일러를 돌려보니 결과는 놀라웠습니다. 텍스처 메모리 사용량은 거의 0에 수렴했고, 픽셀 셰이더 연산 시간도 훨씬 안정적이었죠. 비주얼은 그대로 유지하면서도 프레임은 거의 두 배 가까이 뛰었습니다. 그때 깨달았습니다. '아, 셰이더 코딩은 단순히 비주얼을 만드는 것을 넘어, 시스템 리소스를 정복하는 예술이구나!'

### 6. 결론: 수학으로 GPU를 조련하고, Unity에서 날개를 달아주세요!

'Shader Math & GLSL'은 단순히 멋진 시각 효과를 위한 도구가 아닙니다. 이는 제한된 자원 속에서 최상의 성능을 끌어내는 'Unity Engine Optimization Tricks'의 핵심 무기입니다. 복잡한 문제를 직면했을 때, 무조건 텍스처나 리소스를 줄이려고만 하지 말고, "이걸 수학적으로 어떻게 풀어낼 수 있을까?"라는 질문을 던져보세요.

조금 어렵고 생소하게 느껴질 수 있지만, 벡터, 노이즈, 스텝 함수 등 기본적인 셰이더 수학 개념부터 차근차근 익혀나가면, 어느새 여러분의 GPU는 여러분이 원하는 대로 춤추고, 여러분의 게임은 더 빠르고 아름답게 빛날 것입니다. 이 매혹적인 수학의 세계에 뛰어들어 여러분만의 최적화 마법을 부려보세요! 다음 포스팅에서 또 흥미로운 이야기로 찾아뵙겠습니다. 해피 코딩!
