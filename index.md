---
layout: default
---

굵은 글씨는 **별표 두 개** 로 텍스트를 감싼다.
이탤릭 서체는 _언더바_ 로 텍스트를 감싼다.
취소선 처리는 ~~물결표~~ (물결표) 로 텍스트를 감싼다.

[Link to another page](./another-page.html).

# 머리글1 은 # 을 텍스트 앞 접두어로 사용하며 크기에 따라
## 머리글2 은 ## 두 개
### 머리글3 은 ### 세 개를 앞에 붙혀서 사용한다.


> 블록 처리는 해당 블록 앞에 > 를 붙힌다. 내려쓰기 시, 각 문장 마다 붙혀줘야 한다.
> 여기 앞에도 꺽새 하나
> 여기 앞에도 꺽새 하나 

```js
 (이렇게) ```js
  자바스크립트 코드는 백틱(Option + ~) 세 개 + js 와 백틱 세 개로 문장을 감싼다.
  js 를 쓰지 않으면 javascript 하이라이트가 들어가지 않음.
 var fun = function lang(l) {
    dateformat.i18n = require('./lang/' + l)
    return true;
 }
 (이렇게) ```
```

###### 테이블, 필요 시 raw Text 참조

| head1        | head two          | three |
|:-------------|:------------------|:------|
| ok           | good swedish fish | nice  |
| out of stock | good and plenty   | nice  |
| ok           | good `oreos`      | hmm   |
| ok           | good `zoute` drop | yumm  |

줄 하나 긋기는 별표 세 개를 한 칸 씩 띄워서 쓰면 된다.(* * * 이렇게)
* * *

###### 작은 이미지 첨부

![Octocat](https://github.githubassets.com/images/icons/emoji/octocat.png)

###### 큰 이미지 첨부

![Branching](https://guides.github.com/activities/hello-world/branching.png)
