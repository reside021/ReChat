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
        public bool success { get; set; } // status
        public string token { get; set; } // jwt
        public Data dataUser { get; set; }

    }
}
