---
layout: post
title: "PS1의 저주받은 매력: 텍스처 워블과 정점 스내핑, Unity URP 쉐이더로 완벽 재현하기 (feat. 현대 GPU에 고의로 바보짓 시키기)"
categories: tech
---

### ⚠️ Image Generation Failed
```text
Prompt: A low-polygon, PS1-era 3D character with visibly wobbling textures and jagged edges, standing in a simple, blocky environment. The scene should evoke a nostalgic, slightly glitchy aesthetic, with subtle CRT scanlines or bloom, against a dark, moody background. The character has a determined, quirky expression. The art style should be reminiscent of early 3D console games, yet rendered with a modern, artistic flair, emphasizing the deliberate imperfections.
```


음, 이봐요, 키보드 워리어들! 오늘도 버그와 싸우느라 수고가 많습니다. 하지만 가끔은... 버그를 일부러 만들고 싶지 않나요? 오늘 제가 여러분께 똥손(?)이 아닌 예술혼을 담아 '고의적인 버그'를 구현하는 방법을 전수해 드리겠습니다. 바로, 플스1(PS1) 시절 우리를 울고 웃게 했던 그 놈의 **'텍스처 워블'**과 **'정점 스내핑'**을 현대 Unity URP 쉐이더로 완벽하게 재현하는 기괴하고도 아름다운 기술이죠!

왜 하필 PS1이냐고요? 그 시절 게임들은 낮은 폴리곤과 어딘가 불안한 텍스처가 매력이었습니다. 특히 3D 모델이 춤을 추듯 일렁이고, 텍스처가 젤리처럼 울렁이는 모습은 지금 보면 '버그'지만, 그 당시에는 그래픽카드의 한계가 만들어낸 '독특한 개성'이었죠. 이걸 현대 GPU에 고의로 바보짓 시켜서 레트로 감성을 강탈하는 것, 이게 바로 괴짜 개발자의 로망 아니겠습니까?

---

### **Part 1: 정점 스내핑 (Vertex Snapping) - 계단 현상의 미학**

현대 GPU는 소수점 단위까지 정확하게 정점(Vertex)을 처리합니다. 덕분에 오브젝트가 아무리 멀리 있어도 매끄럽게 보이죠. 하지만 PS1은 달랐습니다. 당시 GPU는 부동 소수점 연산 능력이 매우 제한적이어서, 3D 공간의 정점 좌표를 화면 공간으로 투영할 때 **정수 단위로 반올림**했습니다. 마치 "야, 대충 근처 픽셀에 붙어!"라고 명령하는 것 같았죠. 그 결과 오브젝트의 가장자리가 울퉁불퉁하게 픽셀 그리드에 스냅되는 현상이 발생했고, 마치 계단처럼 각진 실루엣을 만들어냈습니다. 이걸 저는 **"계단 현상의 미학"**이라고 부릅니다.

**원리 파고들기:**
PS1은 정점을 월드 공간 -> 뷰 공간 -> 투영 공간(클립 공간)으로 변환할 때, 중간 단계에서 좌표를 고정 소수점(Fixed-point)으로 처리하거나 최종적으로 화면 공간(Screen space)에 투영된 후 픽셀 단위로 스내핑하는 유사한 효과를 보였습니다. 핵심은 **'소수점 버리기'** 입니다.

**의사코드 (Vertex Shader Logic):**

```glsl
// Vertex Shader (Unity HLSL-like)
V2F vert (appdata_full v)
{
    V2F o;

    // 1. 기본적인 모델-뷰-프로젝션 변환 (클립 공간 좌표 획득)
    float4 clipPos = UnityObjectToClipPos(v.vertex);

    // 2. 클립 공간 X/Y 좌표를 화면 해상도에 맞춰 스케일링 후 정수화
    //    _ScreenSnapFactor는 화면 해상도에 비례하는 적절한 상수 (예: 256.0 ~ 512.0)
    //    이 값을 통해 PS1의 '가상 해상도'를 조절하는 느낌을 낼 수 있습니다.
    float snapX = round(clipPos.x * _ScreenSnapFactor) / _ScreenSnapFactor;
    float snapY = round(clipPos.y * _ScreenSnapFactor) / _ScreenSnapFactor;

    // 3. 스내핑된 X/Y 좌표를 원래 clipPos에 적용
    clipPos.x = snapX;
    clipPos.y = snapY;

    o.vertex = clipPos; // 스내핑된 정점 좌표를 출력
    o.uv = TRANSFORM_TEX(v.uv, _MainTex); // UV는 그대로 전달
    // 기타 필요한 값들 (노멀, 컬러 등)도 평소처럼 전달

    return o;
}
```

이 코드를 적용하면 오브젝트의 윤곽선이 특정 가상 해상도 그리드에 맞춰 '뚝뚝' 끊기는 듯한 효과를 얻을 수 있습니다. 마치 해상도가 낮은 화면에서 보는 듯한 착시를 유발하죠. 현대 GPU에게 고의로 "야, 너 똑똑한 척 하지 말고 대충대충 해봐!"라고 지시하는 겁니다. 캬!

---

### **Part 2: 텍스처 워블 (Texture Wobble) - 젤리 텍스처의 향연**

정점 스내핑이 윤곽선에 대한 것이라면, 텍스처 워블은 텍스처 자체에 대한 것입니다. PS1은 **원근 보정(Perspective Correction) 없는 어파인 텍스처 매핑(Affine Texture Mapping)** 방식을 사용했습니다. 이게 뭔 소리냐고요?

간단히 말해, 현대 GPU는 3D 오브젝트의 멀리 있는 부분은 텍스처를 좁게, 가까이 있는 부분은 넓게 펼쳐서 자연스럽게 보이도록 '원근 보정'을 합니다. 하지만 PS1은 이걸 못했어요. 멀리 있든 가까이 있든 텍스처를 '선형적으로' 늘려서 매핑했죠. 그 결과 카메라에서 멀리 떨어져 있거나 비스듬하게 놓인 폴리곤의 텍스처가 심하게 왜곡되고 일렁이는, 마치 젤리처럼 '워블'하는 현상이 발생했습니다.

**원리 파고들기:**
텍스처 UV 좌표가 화면 공간에서 선형적으로 보간될 때 발생하는 문제입니다. 우리는 이 '잘못된' 보간 방식을 흉내 내기 위해, 이미 완벽하게 보정된 UV에 **고의적인 왜곡**과 **양자화**를 더할 겁니다.

**의사코드 (Fragment Shader Logic):**

```glsl
// Fragment Shader (Unity HLSL-like)
fixed4 frag (V2F i) : SV_Target
{
    // 1. 화면 공간 좌표 획득 (원근 보정되지 않은 UV를 만들 기반)
    //    i.vertex는 클립 공간 좌표이므로, w로 나누어 정규화된 디바이스 좌표를 얻습니다.
    float2 screenPos = i.vertex.xy / i.vertex.w;

    // 2. 텍스처 워블 효과 추가
    //    시간(_Time.y)과 화면 공간 X/Y 좌표를 이용하여 UV에 흔들림 효과를 줍니다.
    //    _WobbleSpeed: 흔들리는 속도, _WobbleFrequency: 흔들림 밀도, _WobbleAmplitude: 흔들림 강도
    float2 wobbleOffset = sin(_Time.y * _WobbleSpeed + screenPos.x * _WobbleFrequency) * _WobbleAmplitude;
    wobbleOffset += cos(_Time.y * _WobbleSpeed * 0.7 + screenPos.y * _WobbleFrequency * 0.5) * _WobbleAmplitude * 0.7;

    float2 perturbedUV = i.uv + wobbleOffset;

    // 3. UV 양자화 (픽셀레이션/뭉개짐 효과 추가)
    //    _UVResolution은 텍스처의 '가상 해상도'를 결정합니다. (예: 128.0)
    //    이 값을 낮출수록 텍스처가 더 뭉개지고 블록처럼 보입니다.
    float2 quantizedUV = floor(perturbedUV * _UVResolution) / _UVResolution;

    // 4. 최종 UV로 텍스처 샘플링
    fixed4 col = tex2D(_MainTex, quantizedUV);

    // 5. 컬러 출력 (필요하다면 안개, 라이팅 등 추가)
    return col;
}
```

이 쉐이더 코드는 현대 GPU가 자동으로 수행하는 UV 보간을 무시하고, 강제로 **'흔들림(wobble)'**과 **'뭉개짐(quantization)'**을 적용합니다. 마치 텍스처가 춤을 추듯 일렁이고, 저해상도 화면에서 이미지가 깨져 보이던 그 시절의 '감성'을 그대로 재현하는 거죠! "이게 버그가 아니라 스타일이다!" 외치세요.

---

### **Unity URP에서 구현하기 (팁: Shader Graph + Custom Function)**

이 모든 마법은 Unity의 URP (Universal Render Pipeline)에서 커스텀 쉐이더나 쉐이더 그래프를 통해 쉽게 구현할 수 있습니다.

*   **Shader Graph:** Vertex Snapping은 Vertex Stage에서 `Custom Function` 노드를 사용해 위의 `vert` 로직을 구현하면 됩니다. Texture Wobble은 Fragment Stage에서 `Custom Function` 노드로 `frag` 로직을 넣거나, `Time`, `Screen Position`, `UV`, `Sin`, `Cos`, `Floor`, `Divide` 등의 노드를 조합하여 직접 만들 수도 있습니다.
*   **Code Shader:** 직접 HLSL 파일을 만들어서 URP Shader Lab 문법에 맞춰 위의 코드를 작성하면 됩니다. 파라미터(`_ScreenSnapFactor`, `_WobbleSpeed` 등)들은 쉐이더 프로퍼티로 노출하여 인스펙터에서 조절할 수 있게 만드세요.

여러분은 이제 고의로 게임을 '망치는' 기술을 습득했습니다! 짝짝짝.

---

### **괴짜 개발자의 유머와 철학: 버그는 때로 예술이 된다!**

가끔 이런 생각을 합니다. 완벽함을 추구하는 것이 과연 개발자의 유일한 미덕일까? PS1 시절의 개발자들은 제한된 하드웨어 속에서 최선을 다했지만, 결국 시스템의 한계가 만들어낸 '불완전함'이 역설적으로 독특한 매력이 되었죠.

현대 게임 엔진은 너무나 강력해서, 이런 '옛날 버그'를 재현하는 게 오히려 더 복잡할 때가 있습니다. 완벽하게 작동하는 시스템에 고의로 바보짓을 시키는 것. 마치 깨끗한 도화지에 일부러 빈티지한 얼룩을 만드는 예술가의 심정과 비슷할까요?

여러분도 가끔은 이 강박적인 '버그 픽스'의 굴레에서 벗어나, 의도적으로 '불완전함의 미학'을 탐구해 보세요. 때로는 가장 엉성해 보이는 버그가 가장 독창적인 아트 스타일이 될 수도 있답니다.

자, 이제 이 기술을 들고 가서 여러분의 게임을 '불완전하게 완벽'하게 만드세요! 분명 여러분의 플레이어들은 "이게 버그인가, 아트인가? 젠장, 너무 멋지잖아!"라고 외칠 겁니다. 아니면 최소한 "음... 뭔가 옛날 게임 같다?"라고 하겠죠! 후후후.
