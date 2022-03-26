using System;
using System.Collections.Generic;
using System.Text;

namespace WSClientDB.dataClasses
{
    class ListDataOfMessage
    {
        public string type { get; set; }
        public string oper { get; set; } // type oper
        public string table { get; set; }
        public bool success { get; set; }
        public List<DataOfMessage> listOfData { get; set; }
        public string tagUser { get; set; }
        public string token { get; set; }
    }
}
