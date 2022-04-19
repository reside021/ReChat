using System;
using System.Collections.Generic;
using System.Text;

namespace WSClientDB.dataClasses
{
    class SuccessCreateUserDlg
    {
        public string type { get; set; }
        public string oper { get; set; } // type oper
        public bool success { get; set; }
        public string dialog_id { get; set; }
        public string userManager { get; set; }
        public List<string> userCompanion { get; set; }
        public int enteredTime { get; set; }
        public int countMsg { get; set; }
        public int lastTimeMsg { get; set; }
        public int typeOfDlg { get; set; }
        public int rang { get; set; }
        public string nameOfChat { get; set; }
    }
}
