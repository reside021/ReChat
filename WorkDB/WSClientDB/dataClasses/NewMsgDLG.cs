using System;
using System.Collections.Generic;
using System.Text;

namespace WSClientDB.dataClasses
{
    class NewMsgDLG
    {
        public string dialog_id { get; set; }
        public string sender { get; set; }
        public string typeMsg { get; set; }
        public string text { get; set; }
        public string receiverId { get; set; }
    }
}
