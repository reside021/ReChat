using System;
using System.Collections.Generic;
using System.Text;

namespace WSClientDB.dataClasses
{
    class ResultActionFrnd
    {
        public string type { get; set; } // RESULTBD
        public string oper { get; set; } // type oper
        public string typeAction { get; set; } // type action with friends
        public string tagUserSender { get; set; } // userSender
        public string nameUserSender { get; set; } // userSender name
        public string tagUserReceiver { get; set; } // userReceiver tag
        public string nameUserReceiver { get; set; } // userReceiver name
        public bool success { get; set; } // status
        public string typeDelete { get; set; } // type delete for client
    }
}
