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
        public string userCompanion { get; set; }
        public string enteredTime { get; set; }
    }
}
