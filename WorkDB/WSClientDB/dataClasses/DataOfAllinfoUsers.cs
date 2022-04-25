using System;
using System.Collections.Generic;
using System.Text;

namespace WSClientDB.dataClasses
{
    class DataOfAllinfoUsers
    {
        public string type { get; set; }
        public string oper { get; set; } // type oper
        public string table { get; set; }
        public bool success { get; set; }
        public Data dataUsers { get; set; }
        public string tagUser { get; set; }
    }
}
