# cola-flow
站在前辈的肩膀上眺望远方，cola框架https://github.com/alibaba/COLA  
本代碼庫是在cola框架基礎上實現的流程編排引擎和組件，最新版是基于cola3.0的版本；
本代码库是底层引擎，应用代码见https://github.com/doufuche/cola-flow-server

簡單描述下背景  
COLA是DDD領域驅動框架，提供了DDD、CQRS、擴展點等功能和規範，具有很好的學習和使用價值  
基于COLA規範，實施過程中可能會遇到以下几方面的問題：  
1，一個業務流程可能有好幾個步驟需要執行，可以封裝為Event執行，或者代碼組裝Event執行流程，但是過程執行失敗怎麽處理?  
2，Event粒度拆分問題，粒度細的話邏輯更清晰，粒度粗的話代碼更簡單  
3，一個業務流程中可能需要修改多個業務數據，并且需要保證修改的一致性  

基于COLA框架實現的流程編排
基於DDD領域驅動和COLA框架，按照COLA規範劃分擴展點，梳理每個領域的鏈路和業務場景，抽取業務身份和業務節點概念，按業務身份串聯業務流程  
1，每個功能為一個節點Event，實現該節點Event的邏輯需要做冪等處理；該Event節點後續還可以升級為MQ節點  
2，每個節點Event對象需要的參數實現從Event上下文獲取，或從DB/RPC獲取，解耦節點與節點之間的強依賴  
3，抽象業務身份和節點Event的關係，通過業務身份去串聯節點Event流程鏈  
4，實現節點Event和流程鏈的管理，後續可實現動態調整或新增節點Event，以快速支持不同行業（業務場景）的目的  

架構設計和使用姿勢待後續補充

