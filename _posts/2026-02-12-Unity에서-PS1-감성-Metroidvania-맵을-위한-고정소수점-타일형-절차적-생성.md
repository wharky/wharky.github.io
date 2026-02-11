---
layout: post
title: "Unity에서 PS1 감성 Metroidvania 맵을 위한 고정소수점 타일형 절차적 생성: 옛것에서 배우는 최적화의 미학"
categories: tech
---

![Unity에서 PS1 감성 Metroidvania 맵을 위한 고정소수점 타일형 절차적 생성: 옛것에서 배우는 최적화의 미학](https://image.pollinations.ai/prompt/A+cluttered%2C+dimly+lit+game+developer%27s+desk%2C+dominated+by+a+retro+CRT+monitor+displaying+a+vibrant%2C+blocky%2C+low-polygon+Metroidvania+map.+Beside+it%2C+a+stack+of+vintage+PS1+game+cases%2C+a+half-empty+coffee+mug%2C+and+a+soldering+iron.+Hands+with+smudges+of+solder+paste+are+furiously+typing+on+a+mechanical+keyboard%2C+while+a+holographic+projection+of+complex+fixed-point+math+formulas+and+a+wireframe+map+blueprint+hovers+above%2C+ethereal+and+glowing+with+digital+light.+The+scene+blends+nostalgic+analog+tech+with+futuristic+digital+creation%2C+capturing+the+essence+of+a+quirky+genius+breathing+new+life+into+old-school+mechanics.?width=800&height=450&nologo=true&seed=8534)

> **AI Image Prompt:** A cluttered, dimly lit game developer's desk, dominated by a retro CRT monitor displaying a vibrant, blocky, low-polygon Metroidvania map. Beside it, a stack of vintage PS1 game cases, a half-empty coffee mug, and a soldering iron. Hands with smudges of solder paste are furiously typing on a mechanical keyboard, while a holographic projection of complex fixed-point math formulas and a wireframe map blueprint hovers above, ethereal and glowing with digital light. The scene blends nostalgic analog tech with futuristic digital creation, capturing the essence of a quirky genius breathing new life into old-school mechanics.


자, 친애하는 괴짜 천재 동지들! 오늘도 이 미친 세상에서 비트와 바이트를 주무르며 인류의 유희 수준을 한 단계 끌어올리고자 고뇌하는 그대들에게, 감히 이 몸이 던지는 엉뚱한 제안이 하나 있습니다. 아, 물론 농담은 아닙니다. 저는 *언제나* 진지합니다. 그 진지함이 조금… 독특할 뿐이죠.

우리가 오늘 파헤칠 주제는 바로 이것입니다: **PS1 감성이 충만한 Metroidvania 맵을 Unity에서 절차적으로 생성하되, 고대 유물 같은 '고정소수점' 연산을 사용하여 개발자에게 잊혔던 최적화의 미학을 일깨우는 것!**
"엥? 고정소수점? 그걸 왜 또 써요? float가 있는데?" 라고 생각했다면, 당신은 아직 저의 '엉뚱한 비전'에 도달하지 못한 겁니다. 후후후…

### 1. 왜 하필 PS1? 왜 고정소수점? (진짜 미친 짓인가?)

우선, PS1 이야기를 해봅시다. 이 친구, 정말 기묘한 하드웨어였습니다. 3D 게임의 시대를 열었지만, 현대 GPU처럼 강력한 부동소수점 연산 장치(FPU)가 없거나 매우 느렸죠. 그래서 당시 개발자들은 미친 듯이 똑똑하게도 **정수형(Integer) 변수 안에 소수점 위치를 고정시켜 계산하는 '고정소수점(Fixed-Point)' 연산**을 애용했습니다.

*   **성능:** FPU 호출 오버헤드 없이 순수 정수 연산만으로 소수를 다룰 수 있었죠. 이는 느린 하드웨어에서 프레임을 쥐어짜 내는 생존 전략이었습니다.
*   **정확성:** 부동소수점의 미묘한 정밀도 문제(epsilon 값과의 전쟁, 부동소수점 오차 누적 등)를 피할 수 있어, 특히 네트워크 동기화가 중요한 게임에서 예측 가능한 결과(Determinism)를 보장하는 데 유리합니다.
*   **감성:** PS1 게임 특유의 '삐걱거리는 듯하면서도 정밀한' 움직임, 미묘하게 어긋나는 카메라 워크, 그리고 특정 연산의 '거친' 느낌이 바로 이 고정소수점에서 기인하기도 합니다. 우리는 이 감성을 절차적 생성에까지 확장시킬 겁니다!

자, 이제 이 고정소수점 방식을 우리의 Metroidvania 맵 생성 알고리즘에 적용해봅시다. 타일 기반 맵에서 '정수' 좌표를 주로 쓰는데 굳이? 라고 생각할 수 있겠죠. 하지만 맵의 '방' 크기를 결정하거나, 방과 방 사이의 복잡한 연결 경로를 계산하거나, 혹은 나중에 오브젝트를 배치할 때 미묘한 '오프셋'을 주는 등의 *내부적인 계산*에는 여전히 부동소수점이 개입할 여지가 많습니다. 이때 고정소수점을 사용하면 PS1 시절의 계산 감성을 유지하면서, 동시에 정밀도를 신경 쓰는 개발자의 고뇌를 다시금 맛볼 수 있습니다. 정말 멋지지 않습니까?

### 2. 절차적 생성 알고리즘에 고정소수점 한 스푼 (혹은 반 스푼?)

우리의 목표는 Metroidvania 스타일의 '탐험 가능한' 맵을 만드는 것입니다. 큰 그림은 다음과 같습니다:
1.  **핵심 방 배치**: 시작 지점, 보스 방, 주요 아이템 방 등 핵심적인 방들을 먼저 고정소수점 좌표와 크기로 배치합니다.
2.  **방 확장/연결**: 핵심 방들로부터 '취한 자의 행진(Drunkard's Walk)' 알고리즘처럼 무작위로 경로를 확장하거나, 특정 방들을 연결하는 복도를 생성합니다. 이 과정의 '거리 계산'이나 '확장 크기'에 고정소수점을 적용합니다.
3.  **유효성 검사**: 모든 핵심 방이 연결되었는지, 맵이 너무 밀집되거나 텅 비지 않았는지 확인하고 조정합니다.
4.  **타일화 및 디테일**: 최종적으로 고정소수점 좌표를 정수 타일 좌표로 변환하여 Unity `Tilemap`에 그립니다.

여기서 핵심은 **'내부적으로는 고정소수점을 사용하지만, 최종적으로는 정수 타일에 스냅(Snap)시키는 것'** 입니다. 마치 PS1 게임이 내부적으로는 복잡한 행렬 연산을 했지만, 결국 화면에는 픽셀 단위로 그려졌던 것처럼 말이죠.

#### 의사코드: FixedPoint Struct와 그 활용

먼저, 우리만의 `FixedPoint` 구조체를 정의해야 합니다. Unity (C#) 환경에서 `int`나 `long`을 사용하여 구현할 수 있습니다. 저는 주로 `long`을 선호하는데, 더 넓은 범위와 정밀도를 제공하기 때문입니다.

```csharp
// 괴짜 천재 개발자 노트:
// 세상에 float가 판을 칠 때, 우리는 long으로 소수점을 다루는 반항아가 됩니다.
// FRACTIONAL_BITS는 소수점 이하 몇 비트를 쓸지 결정합니다. 16비트면 꽤 정밀하죠.
public const int FRACTIONAL_BITS = 16;
public const long ONE = 1L << FRACTIONAL_BITS; // Fixed-Point에서 1.0f를 나타내는 값

public struct FixedPoint
{
    public long RawValue; // 실제 정수 값

    // 생성자: 정수에서 FixedPoint
    public FixedPoint(long value) { RawValue = value; }

    // float에서 FixedPoint로 변환 (외부 입력용)
    public static FixedPoint FromFloat(float f) {
        return new FixedPoint((long)(f * ONE));
    }

    // int에서 FixedPoint로 변환 (정수 입력용)
    public static FixedPoint FromInt(int i) {
        return new FixedPoint((long)i << FRACTIONAL_BITS);
    }

    // FixedPoint에서 float로 변환 (렌더링/외부 출력용)
    public float ToFloat() {
        return (float)RawValue / ONE;
    }

    // FixedPoint에서 int로 변환 (타일 좌표 등)
    public int ToInt() {
        return (int)(RawValue >> FRACTIONAL_BITS);
    }

    // --- 연산자 오버로드 (이게 진짜 마법이죠!) ---
    public static FixedPoint operator +(FixedPoint a, FixedPoint b) { return new FixedPoint(a.RawValue + b.RawValue); }
    public static FixedPoint operator -(FixedPoint a, FixedPoint b) { return new FixedPoint(a.RawValue - b.RawValue); }
    
    // 곱셈: (a * b) >> FRACTIONAL_BITS 로 정밀도를 유지하며 곱합니다.
    public static FixedPoint operator *(FixedPoint a, FixedPoint b) {
        return new FixedPoint((a.RawValue * b.RawValue) >> FRACTIONAL_BITS);
    }
    
    // 나눗셈: (a << FRACTIONAL_BITS) / b 로 정밀도를 유지하며 나눕니다.
    public static FixedPoint operator /(FixedPoint a, FixedPoint b) {
        // 0으로 나누는 건... 게임을 터뜨리는 가장 쉬운 방법이죠.
        if (b.RawValue == 0) throw new DivideByZeroException("으악! FixedPoint로 0을 나누지 마세요! 정신 나간 짓입니다!");
        return new FixedPoint((a.RawValue << FRACTIONAL_BITS) / b.RawValue);
    }

    // 비교 연산자 (없으면 섭섭하죠)
    public static bool operator <(FixedPoint a, FixedPoint b) { return a.RawValue < b.RawValue; }
    public static bool operator >(FixedPoint a, FixedPoint b) { return a.RawValue > b.RawValue; }
    // ... 다른 연산자들도 추가 가능
}
```

이제 이 `FixedPoint` 구조체를 활용하여 방의 크기나 위치를 결정하는 부분에 적용해봅시다.

```csharp
// 괴짜 천재 개발자 노트:
// 우리가 만드는 방은 '정확'하면서도 미묘하게 'PS1스러운' 불확실성을 가집니다.
// 그게 고정소수점의 매력이죠!

public class RoomDefinition
{
    public FixedPoint X, Y;           // 방의 고정소수점 위치 (센터 기준)
    public FixedPoint Width, Height;  // 방의 고정소수점 너비/높이

    public int TileGridX { get { return X.ToInt(); } } // 최종 타일 그리드 X
    public int TileGridY { get { return Y.ToInt(); } } // 최종 타일 그리드 Y
    public int TileGridWidth { get { return Width.ToInt(); } } // 최종 타일 그리드 너비
    public int TileGridHeight { get { return Height.ToInt(); } } // 최종 타일 그리드 높이
    
    // 방의 타입 (시작, 보스, 일반 등)
    public RoomType Type; 
    public bool IsConnected; // 연결 여부
}

public class MetroidvaniaGenerator
{
    public FixedPoint MinRoomSize = FixedPoint.FromFloat(5.5f);  // 최소 방 크기
    public FixedPoint MaxRoomSize = FixedPoint.FromFloat(20.5f); // 최대 방 크기
    public FixedPoint CorridorThickness = FixedPoint.FromFloat(3.0f); // 복도 두께

    private List<RoomDefinition> rooms = new List<RoomDefinition>();
    private int[,] tileMapGrid; // 최종 타일 맵 (정수형)

    public void GenerateMap(int seed, int mapWidthTiles, int mapHeightTiles)
    {
        UnityEngine.Random.InitState(seed); // 시드 고정으로 동일한 맵 생성
        tileMapGrid = new int[mapWidthTiles, mapHeightTiles];

        // 1. 핵심 방 배치 (고정소수점 사용!)
        RoomDefinition startRoom = CreateRoom(
            FixedPoint.FromFloat(mapWidthTiles / 4.0f),
            FixedPoint.FromFloat(mapHeightTiles / 4.0f),
            MinRoomSize + FixedPoint.FromFloat(UnityEngine.Random.value) * (MaxRoomSize - MinRoomSize),
            RoomType.Start
        );
        rooms.Add(startRoom);

        RoomDefinition bossRoom = CreateRoom(
            FixedPoint.FromFloat(mapWidthTiles * 3.0f / 4.0f),
            FixedPoint.FromFloat(mapHeightTiles * 3.0f / 4.0f),
            MinRoomSize + FixedPoint.FromFloat(UnityEngine.Random.value) * (MaxRoomSize - MinRoomSize),
            RoomType.Boss
        );
        rooms.Add(bossRoom);

        // 2. 방 확장 및 연결 (여기서 거리 계산 등에 고정소수점 활용)
        // 예를 들어, 방과 방 사이의 유클리드 거리 계산
        FixedPoint distance = FixedPoint.FromFloat(
            Mathf.Sqrt(
                (startRoom.X - bossRoom.X).ToFloat() * (startRoom.X - bossRoom.X).ToFloat() +
                (startRoom.Y - bossRoom.Y).ToFloat() * (startRoom.Y - bossRoom.Y).ToFloat()
            )
        );
        // (괴짜 개발자 주: 사실 FixedPoint 내부에 Sqrt를 구현하는 게 '진짜' 고정소수점 감성이지만,
        // 여기서는 편의상 float으로 변환 후 다시 FixedPoint로 가져왔습니다. 죄송합니다. 반성하겠습니다.)

        // 복도 생성 (여기서 FixedPoint로 계산된 CorridorThickness를 사용하여 복도 타일 범위 결정)
        GenerateCorridor(startRoom, bossRoom);

        // 3. 타일화: 최종적으로 Rooms 리스트를 순회하며 tileMapGrid에 타일을 채웁니다.
        foreach (var room in rooms)
        {
            // 고정소수점 값을 다시 정수형 타일 좌표로 변환하여 맵에 그립니다.
            for (int x = room.TileGridX - room.TileGridWidth / 2; x < room.TileGridX + room.TileGridWidth / 2; x++)
            {
                for (int y = room.TileGridY - room.TileGridHeight / 2; y < room.TileGridY + room.TileGridHeight / 2; y++)
                {
                    if (x >= 0 && x < mapWidthTiles && y >= 0 && y < mapHeightTiles)
                    {
                        tileMapGrid[x, y] = 1; // 1은 벽 또는 바닥 타일
                    }
                }
            }
        }
        // ... tileMapGrid를 Unity Tilemap으로 변환하는 로직
    }

    private RoomDefinition CreateRoom(FixedPoint x, FixedPoint y, FixedPoint size, RoomType type)
    {
        return new RoomDefinition
        {
            X = x, Y = y,
            Width = size, Height = size, // 일단 정사각형으로 시작
            Type = type,
            IsConnected = false
        };
    }

    private void GenerateCorridor(RoomDefinition from, RoomDefinition to)
    {
        // 간단한 A* 또는 직선 경로 알고리즘을 여기 넣습니다.
        // 경로를 계산할 때, 각 노드 간의 '비용'이나 '거리'를 FixedPoint로 계산하여 저장합니다.
        // 예를 들어, 길을 뚫을 때 시작점과 끝점의 FixedPointX, FixedPointY를 기준으로 
        // 중간 지점의 FixedPoint를 계산하여 복도를 만듭니다.
        
        // Pseudo-code for a simple straight corridor (for demonstration)
        int startX = from.TileGridX;
        int startY = from.TileGridY;
        int endX = to.TileGridX;
        int endY = to.TileGridY;

        // Horizonal path
        for (int x = Math.Min(startX, endX); x <= Math.Max(startX, endX); x++) {
            // CorridorThickness.ToInt() 만큼의 두께로 맵에 타일 채우기
            for(int dy = -(CorridorThickness.ToInt() / 2); dy <= (CorridorThickness.ToInt() / 2); dy++) {
                if(startY + dy >= 0 && startY + dy < tileMapGrid.GetLength(1))
                    tileMapGrid[x, startY + dy] = 1;
            }
        }
        // Vertical path
        for (int y = Math.Min(startY, endY); y <= Math.Max(startY, endY); y++) {
             for(int dx = -(CorridorThickness.ToInt() / 2); dx <= (CorridorThickness.ToInt() / 2); dx++) {
                if(endX + dx >= 0 && endX + dx < tileMapGrid.GetLength(0))
                    tileMapGrid[endX + dx, y] = 1;
            }
        }
        // 이 부분에서 FixedPoint 값을 가지고 복도 너비 등을 미묘하게 조절하여
        // "타일에 정확히 맞지 않는" 듯한 PS1 감성을 연출할 수 있습니다.
        // (예: CorridorThickness.ToFloat() * pixel_per_unit 를 사용해 실제 게임 오브젝트에 적용)
    }
}
```

### 3. Unity Engine Tricks: 레트로 감성 한도 초과

Unity에서 이 모든 것을 구현하는 것은 몇 가지 트릭만 알면 쉽습니다. (물론 '쉬운' 기준은 저 같은 괴짜 개발자 기준입니다.)

1.  **커스텀 FixedPoint 구조체**: 위에서 보셨듯이, C# `struct`를 활용하여 `FixedPoint` 타입을 만듭니다.
2.  **`[ExecuteInEditMode]`**: 맵 생성 스크립트에 이 속성을 추가하면, 플레이 모드에 진입하지 않고도 Unity 에디터에서 실시간으로 맵이 생성되는 것을 확인할 수 있습니다. 마치 고대의 마법사가 마법진을 그리는 것처럼 말이죠.
3.  **Unity Tilemap**: 생성된 `tileMapGrid` (정수 배열)를 Unity의 `Tilemap` 컴포넌트에 매핑하여 시각적으로 표현합니다. 이 친구는 2D 타일 맵을 다루는 데 최적화되어 있습니다.
4.  **`ScriptableObject`**: 맵 생성 파라미터 (최소 방 크기, 최대 방 크기, 복도 두께, 시드 등)를 `ScriptableObject`에 저장하세요. 이렇게 하면 매번 스크립트를 건드릴 필요 없이 에디터에서 쉽게 값을 변경하며 다양한 맵을 실험할 수 있습니다.

### 마치며 (비트의 노래)

자, 이제 PS1 시절의 고정소수점 연산을 활용하여 Unity에서 Metroidvania 맵을 절차적으로 생성하는 이 기묘한 여정을 이해하셨을 겁니다. 이 방법은 단순히 과거의 기술을 답습하는 것이 아닙니다. **제한된 환경에서 최적의 결과를 이끌어내기 위한 선배 개발자들의 지혜를 배우고, 그것을 현대 엔진에서 재해석하는 '창조적 파괴' 행위입니다.**

이 고정소수점 덕분에 여러분의 게임은 남들과는 다른, 미묘하게 '맛이 다른' 물리 연산이나 움직임을 가질 수도 있습니다. 어쩌면 그게 바로 플레이어가 "어? 이 게임 뭔가 다른데?" 라고 느끼게 할, 여러분만의 비밀 무기가 될 지도 모릅니다.

그러니 오늘 밤, 치킨을 시키고, 커피를 내리고, 이 `FixedPoint` 구조체를 씹고 뜯고 맛보고 즐기며, 낡은 기술 속에서 새로운 영감을 찾는 저와 같은 괴짜가 되기를 바랍니다. 다음엔 더 기묘하고, 더 실용적이며, 더 개발자적인 주제로 돌아오겠습니다. 아듀!
