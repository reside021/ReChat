using System;
using System.Collections.Generic;
using System.Text;

namespace WSClientDB.dataClasses
{
    class SuccessUpdate
    {
        public string type { get; set; }
        public string oper { get; set; } // type oper
        public string typeUpdate { get; set; } // where updating
        public bool success { get; set; }
        public string tagId { get; set; }
        public string newName { get; set; }
        public bool isVisible { get; set; }
        public int countMsg { get; set; }
        public string dialog { get; set; }
        public string needTagUser { get; set; }
    }
}
