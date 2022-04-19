using System;
using System.Collections.Generic;
using System.Text;

namespace WSClientDB.dataClasses
{
    class SuccessInsertMsgDlg
    {
        public string type { get; set; }
        public string oper { get; set; } // type oper
        public bool success { get; set; }
        public string dialog_id { get; set; }
        public string sender { get; set; }
        public string typeMsg { get; set; }
        public string textMsg { get; set; }
        public string timeCreated { get; set; }
        public string receiverId { get; set; }
        public List<string> listReceiverId { get; set; }
    }
}
