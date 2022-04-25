using System;
using System.Collections.Generic;
using System.Text;

namespace WSClientDB.dataClasses
{
    struct Data
    {
        public string nickname { get; set; } // name
        public string tagUser { get; set; } // uId
        public bool isVisible { get; set; } // isVisible for all
        public bool isAvatar { get; set; } // have avatar?
        public int isVisionData { get; set; }
        public int gender { get; set; }
        public string birthday { get; set; }
        public string socStatus { get; set; }
        public string country { get; set; }
        public string dateReg { get; set; }
        public string aboutMe { get; set; }
    }
}
