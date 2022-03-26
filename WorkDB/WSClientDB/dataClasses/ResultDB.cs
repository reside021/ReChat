using System;
using System.Collections.Generic;
using System.Text;

namespace WSClientDB.dataClasses
{
    class ResultDB
    {
        public string type { get; set; } // RESULTBD
        public string oper { get; set; } // type oper
        public string authorId { get; set; } // authorId
        public string nickName { get; set; } // name
        public string tag { get; set; } // uId
        public bool isVisible { get; set; } // isVisible for all
        public bool isAvatar { get; set; } // have avatar?
        public bool success { get; set; } // status
        public string token { get; set; } // jwt
    }
}
