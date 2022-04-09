using System;
using System.Collections.Generic;
using System.Text;

namespace WSClientDB.dataClasses
{
    class UpdateCountMsg
    {
        public string tagUser { get; set; }
        public string dialog { get; set; }
        public string needTagUser { get; set; }
        public string countMsg { get; set; }
    }
}
