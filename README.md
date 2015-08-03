# FoxBukkitChatLink

```
┌ ─ ─ ─ ─ ─ ┐     ┌ ─ ─ ─ ─ ─ ┐    ┌───────────┐     ┌───────────┐    ┌ ─ ─ ─ ─ ─ ┐     ┌ ─ ─ ─ ─ ─ ┐
  MC Client         MC Client      │ MC Client │     │ MC Client │      MC Client         MC Client  
└ ─ ─ ─ ─ ─ ┘     └ ─ ─ ─ ─ ─ ┘    └───────────┘     └───────────┘    └ ─ ─ ─ ─ ─ ┘     └ ─ ─ ─ ─ ─ ┘
      ▲                 ▲                ▲                 ▲                ▲                 ▲      
                                         │                 │                                         
      └ ─ ─ ─ ─ ─ ─ ─ ─ ┘                └────────┬────────┘                └ ─ ─ ─ ─ ─ ─ ─ ─ ┘      
               │                                  │                                  │               
                                                  │                                                  
               ▼                                  ▼                                  ▼               
┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐    ┌─────────────────────────────┐    ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐
                                   │                             │                                   
│          MC Server          │    │          MC Server          │    │          MC Server          │
                                   │                             │                                   
├ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┤    ├──────────────┬──────────────┤    ├ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┤
    CMO sub    │   CMI push        │   CMO sub    │   CMI push   │        CMO sub    │   CMI push    
└ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘    └──────────────┴──────────────┘    └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘
        ▲                                  ▲              │                   ▲              │       
        │           │                      │              │                   │                      
         ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┼ ─ ─ ─ ─ ─ ─ ─│─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘       
                                           │              │                                          
                                           │              ▼                                          
                                   ┌──────────────┬──────────────┐                                   
                           ┌───────│   CMO pub    │   CMI pull   │◀─────┐                            
                           │       ├──────────────┴──────────────┤      │                            
                           │       │                             │      │                            
                           │       │       ChatLinkWorker        │      │                            
                           │       │      (can be multiple)      │      │                            
                           │       │                             │      │                            
                           │       └─────────────────────────────┘      │                            
                           │                                            │                            
                           ▼                                            │                            
                   ┌──────────────┐                             ┌──────────────┐                     
                   │   CMO sub    │                             │   CMI push   │                     
                   ├──────────────┤    ┌────────────────────┐   ├──────────────┤                     
                   │              │    │                    │   │              │                     
                   │  API Daemon  │───▶│ API message cache  │──▶│  API Server  │                     
                   │              │    │                    │   │              │                     
                   └──────────────┘    └────────────────────┘   ├──────────────┤                     
                                                                │  JSON HTTP   │                     
  ┌────────────────────────────────────────┐                    └──────────────┘                     
  │                                        │                            ▲                            
  │CMO: ChatMessageOut                     │         ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─│─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐       
  │Formatted chat message with parameters  │         ▼                  ▼                    ▼       
  │                                        │ ┌ ─ ─ ─ ─ ─ ─ ─    ┌──────────────┐     ┌ ─ ─ ─ ─ ─ ─ ─ 
  │CMI: ChatMessageIn                      │    JSON HTTP   │   │  JSON HTTP   │        JSON HTTP   │
  │Raw chat message from a user            │ ├ ─ ─ ─ ─ ─ ─ ─    ├──────────────┤     ├ ─ ─ ─ ─ ─ ─ ─ 
  │                                        │                │   │              │                    │
  │ProtoBuf used for encoding              │ │  API Client      │  API Client  │     │  API Client   
  │                                        │                │   │              │                    │
  └────────────────────────────────────────┘ └ ─ ─ ─ ─ ─ ─ ─    └──────────────┘     └ ─ ─ ─ ─ ─ ─ ─ 
```
