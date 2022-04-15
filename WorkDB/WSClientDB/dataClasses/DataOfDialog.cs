using System;
using System.Collections.Generic;
using System.Text;

namespace WSClientDB.dataClasses
{
    class DataOfDialog
    {
        public string dialog_id { get; set; }
        public string tagUser { get; set; }
        public string enteredTime { get; set; }
        public int countMsg { get; set; }
        public int lastTimeMsg { get; set; }
    }
}
